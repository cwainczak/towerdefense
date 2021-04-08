package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;

import com.google.android.material.math.MathUtils;
import com.wsu.towerdefense.audio.BasicSoundPlayer;
import com.wsu.towerdefense.audio.SoundSource;
import java.util.List;

import static com.google.android.material.math.MathUtils.lerp;

public class Projectile extends AbstractMapObject implements SoundSource {

    // What percent of the bitmap height will be used for the hitbox
    private static final float hitboxScaleY = 0.8f;

    // What percent of the bitmap width will be used for the hitbox
    private static final float hitboxScaleX = 0.8f;

    public enum Type {

        LINEAR(
            1000f,
            10,
            false,
            R.mipmap.projectile_1,
            -1,
            -1
        ),
        HOMING(
            750f,
            15,
            true,
            R.mipmap.projectile_2,
            R.raw.game_rocket_travel,
            R.raw.game_rocket_explode
        );

        final float speed;
        final int damage;
        final int imageID;
        final int travelSoundID;
        final int impactSoundID;
        final boolean armorPiercing;

        /**
         * @param speed         The speed of the projectile
         * @param damage        Damage done to an Enemy hit by this projectile
         * @param imageID       The Resource ID of the image of the projectile
         * @param speed         The speed of the projectile
         * @param damage        Damage done to an Enemy hit by this projectile
         * @param armorPiercing Whether or not this {@link Projectile.Type } can pierce {@link Enemy
         *                      } armor
         */
        Type(float speed, int damage, boolean armorPiercing, int imageID, int travelSoundID,
            int impactSoundID) {
            this.speed = speed;
            this.damage = damage;
            this.armorPiercing = armorPiercing;
            this.imageID = imageID;
            this.travelSoundID = travelSoundID;
            this.impactSoundID = impactSoundID;
        }
    }

    private final BasicSoundPlayer audioTravel;
    private final BasicSoundPlayer audioImpact;

    public final Type type;
    private final Enemy target;
    private float velX;     // Velocity X of the LINEAR type projectiles, based on the target's initial position
    private float velY;     // Velocity Y of the LINEAR type projectiles, based on the target's initial position
    public boolean remove;

    private final float speedModifier;
    private final float damageModifier;

    /**
     * A projectile shot by Towers at Enemies
     *
     * @param location A PointF representing the location of the bitmap's center
     * @param target   The Enemy this projectile is targeting (if none, value is null)
     */
    public Projectile(Context context, PointF location, Type pt, Enemy target,
        float speedModifier,
        float damageModifier) {
        super(context, location, pt.imageID);

        this.type = pt;
        this.target = target;
        this.speedModifier = speedModifier;
        this.damageModifier = damageModifier;

        if (this.type == Type.LINEAR) {
            PointF newVel = Util.getNewVelocity(
                this.location, this.target.location, getEffectiveSpeed()
            );
            this.velX = newVel.x;
            this.velY = newVel.y;
        }

        this.audioTravel = type.travelSoundID >= 0
            ? new BasicSoundPlayer(context, type.travelSoundID, true)
            : null;
        this.audioImpact = type.impactSoundID >= 0
            ? new BasicSoundPlayer(context, type.impactSoundID, true)
            : null;

        if (this.audioTravel != null) {
            this.audioTravel.play(context, Settings.getSFXVolume(context));
        }
    }

    public void update(Game game, double delta) {
        if (this.type == Type.HOMING) {
            double distanceToTarget = Math.hypot(Math.abs(location.x - target.location.x),
                Math.abs(location.y - target.location.y));
            double distanceMoved = getEffectiveSpeed() * delta;

            // If the projectile moved far enough to reach the target set it at the target location
            if (distanceMoved >= distanceToTarget) {
                location.set(target.location);
            } else {
                // Otherwise move the projectile towards the target
                location.set(calculateNewLocation(delta));
            }
        } else if (this.type == Type.LINEAR) {
            location.set(calculateNewLocation(delta));
        }

        checkCollision(game.getContext(), game.getEnemies());

        if (isOffScreen(game.getGameWidth(), game.getGameHeight())) {
            remove(game.getContext());
        }
    }

    @Override
    public void render(double lerp, Canvas canvas, Paint paint) {
        if (!remove) {
            PointF newLoc = calculateNewLocation(lerp);

            Matrix matrix = new Matrix();
            matrix.postRotate((float) Util.getAngleBetweenPoints(newLoc, target.location) + 90,
                bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);

            matrix.postTranslate(newLoc.x - bitmap.getWidth() / 2f,
                newLoc.y - bitmap.getHeight() / 2f);

            canvas.drawBitmap(bitmap, matrix, null);
        }
    }

    private void checkCollision(Context context, List<Enemy> enemies) {
        for (Enemy e : enemies) {
            if (e.collides(location.x, location.y,
                bitmap.getWidth() * hitboxScaleX,
                bitmap.getHeight() * hitboxScaleY)
            ) {
                e.hitByProjectile(this);
                remove(context);
                break;
            }
        }
    }

    /**
     * Calculates the Projectile's new position given a change in time
     *
     * @param delta The change in time since this Projectile's location has been updated
     * @return The new location this Projectile should move to
     */
    private PointF calculateNewLocation(double delta) {
        switch (this.type) {
            case HOMING: {
                double distanceToTarget = Math.hypot(
                    Math.abs(location.x - target.location.x),
                    Math.abs(location.y - target.location.y)
                );
                double distanceMoved = getEffectiveSpeed() * delta;

                float amount = (float) (distanceMoved / distanceToTarget);

                return new PointF(
                    MathUtils.lerp(location.x, target.location.x, amount),
                    MathUtils.lerp(location.y, target.location.y, amount)
                );
            }
            case LINEAR: {
                return Util.getNewLoc(this.location, this.velX, this.velY, delta);
            }
            default:
                return null;
        }
    }

    private boolean isOffScreen(int screenWidth, int screenHeight) {
        return this.location.x < 0 || this.location.x > screenWidth ||
            this.location.y < 0 || this.location.y > screenHeight;
    }

    private float getEffectiveSpeed() {
        return this.type.speed * this.speedModifier;
    }

    public float getEffectiveDamage() {
        return this.type.damage * this.damageModifier;
    }

    private void remove(Context context) {
        remove = true;
        if (this.audioImpact != null) {
            this.audioImpact.play(context, Settings.getSFXVolume(context));
        }
        this.release();
    }

    @Override
    public void release() {
        if (this.audioTravel != null) {
            this.audioTravel.release();
        }
        // don't release audioImpact field to allow sound to play after projectile is removed
    }
}
