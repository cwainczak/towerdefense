package com.wsu.towerdefense.Controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;

import com.wsu.towerdefense.AbstractMapObject;
import com.wsu.towerdefense.Model.Game;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;
import com.wsu.towerdefense.Util;
import com.wsu.towerdefense.Controller.audio.BasicSoundPlayer;
import com.wsu.towerdefense.Controller.audio.SoundSource;

import java.util.List;

public class Projectile extends AbstractMapObject implements SoundSource {

    // What percent of the bitmap height will be used for the hitbox
    private static final float hitboxScaleY = 0.8f;

    // What percent of the bitmap width will be used for the hitbox
    private static final float hitboxScaleX = 0.8f;

    public enum Behavior {
        LINEAR,
        HOMING,
        HITSCAN
    }

    public enum Type {
        BALL(
            1000f,
            10,
            false,
            0.0,
            1.0,
            R.mipmap.projectile_1,
            -1,
            -1,
            Behavior.LINEAR
        ),
        ROCKET(
            750f,
            15,
            true,
            0.0,
            1.0,
            R.mipmap.projectile_2,
            R.raw.game_rocket_travel,
            R.raw.game_rocket_explode,
            Behavior.HOMING
        ),
        BIG_ROCKET(
            550f,
            20,
            true,
            0.0,
            1.0,
            R.mipmap.projectile_3,
            R.raw.game_rocket_travel,
            R.raw.game_rocket_explode,
            Behavior.HOMING
        ),
        HITSCAN(
            0f,
            20,
            true,
            0.0,
            1.0,
            R.mipmap.projectile_1, // image has no effect
            -1,
            -1,
            Behavior.HITSCAN
        ),
        SLOW(
            1000f,
            2,
            false,
            2.0,
            0.5,
            R.mipmap.projectile_5,
            -1,
            -1,
            Behavior.LINEAR
            );

        final float speed;
        final int damage;
        final int imageID;
        final int travelSoundID;
        final int impactSoundID;
        final boolean armorPiercing;
        public final double slowEnemyTime;
        public final double slowRate;
        final Behavior behavior;

        /**
         * @param speed         The speed of the projectile
         * @param damage        Damage done to an Enemy hit by this projectile
         * @param armorPiercing Whether or not this {@link Type } can pierce {@link Enemy} armor
         * @param slowEnemyTime How long this {@link Type } will reduce {@link Enemy} speed for
         * @param slowRate      The value {@link Enemy} speed will be mulitplied by
         * @param imageID       The Resource ID of the image of the projectile
         */
        Type(float speed, int damage, boolean armorPiercing, double slowEnemyTime, double slowRate, int imageID, int travelSoundID,
             int impactSoundID, Behavior behavior) {
            this.speed = speed;
            this.damage = damage;
            this.armorPiercing = armorPiercing;
            this.slowEnemyTime = slowEnemyTime;
            this.slowRate = slowRate;
            this.imageID = imageID;
            this.travelSoundID = travelSoundID;
            this.impactSoundID = impactSoundID;
            this.behavior = behavior;
        }

        public boolean isArmorPiercing(){
            return armorPiercing;
        }
    }

    private static final int IMAGE_ANGLE = 90;

    private final BasicSoundPlayer audioTravel;
    private final BasicSoundPlayer audioImpact;

    public final Type type;
    private final Enemy target;
    private float velX;
    private float velY;
    public boolean remove;

    private final float speedModifier;
    private final float damageModifier;
    private final double slowTime;
    private final double slowRate;

    public Projectile(Context context, PointF location, Type type, Enemy target, float angle,
        float speedModifier,
        float damageModifier,
        double slowTime,
        double slowRate) {
        super(context, location, type.imageID);

        this.type = type;
        this.target = target;
        this.speedModifier = speedModifier;
        this.damageModifier = damageModifier;
        this.slowTime = slowTime;
        this.slowRate = slowRate;

        this.velX = (float) (getEffectiveSpeed() * Math.cos(Math.toRadians(angle)));
        this.velY = (float) (getEffectiveSpeed() * Math.sin(Math.toRadians(angle)));

        this.audioTravel = this.type.travelSoundID >= 0
            ? new BasicSoundPlayer(context, this.type.travelSoundID, true)
            : null;
        this.audioImpact = this.type.impactSoundID >= 0
            ? new BasicSoundPlayer(context, this.type.impactSoundID, true)
            : null;

        if (this.audioTravel != null) {
            this.audioTravel.play(context, Settings.getSFXVolume(context));
        }
    }

    public void update(Game game, double delta) {
        switch (this.type.behavior) {
            case HOMING: {
                if (this.target.isAlive()) {
                    double angle = Util.getAngleBetweenPoints(this.location, this.target.getLocation());

                    this.velX = (float) (getEffectiveSpeed() * Math.cos(Math.toRadians(angle)));
                    this.velY = (float) (getEffectiveSpeed() * Math.sin(Math.toRadians(angle)));
                }
                this.location.offset((float) (this.velX * delta), (float) (this.velY * delta));
                break;
            }
            case LINEAR: {
                this.location.offset((float) (this.velX * delta), (float) (this.velY * delta));
                break;
            }
            case HITSCAN: {
                if (this.target.isAlive()) {
                    this.target.hitByProjectile(this);
                }
                remove(game.getContext());
                break;
            }
            default: {
                return;
            }
        }

        checkCollision(game.getContext(), game.getEnemies());

        if (isOffScreen(game.getGameWidth(), game.getGameHeight())) {
            remove(game.getContext());
        }
    }

    @Override
    public void render(double lerp, Canvas canvas, Paint paint) {
        if (!remove) {
            Matrix matrix = new Matrix();
            matrix.postRotate(
                (float) (Math.toDegrees(Math.atan2(this.velY, this.velX)) + IMAGE_ANGLE),
                bitmap.getWidth() / 2f,
                bitmap.getHeight() / 2f
            );

            PointF interpolatedLocation = new PointF(
                (float) (this.location.x + this.velX * lerp),
                (float) (this.location.y + this.velY * lerp)
            );
            matrix.postTranslate(
                interpolatedLocation.x - bitmap.getWidth() / 2f,
                interpolatedLocation.y - bitmap.getHeight() / 2f
            );

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

    public double getSlowTime() { return slowTime; }

    public double getSlowRate() { return slowRate; }

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
