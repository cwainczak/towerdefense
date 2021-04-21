package com.wsu.towerdefense.Controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import com.wsu.towerdefense.AbstractMapObject;
import com.wsu.towerdefense.Controller.audio.BasicSoundPlayer;
import com.wsu.towerdefense.Controller.audio.SoundSource;
import com.wsu.towerdefense.Controller.tower.Tower;
import com.wsu.towerdefense.Model.Game;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;
import com.wsu.towerdefense.Util;
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
            -1,
            false,
            R.mipmap.projectile_1,
            -1,
            -1,
            Behavior.LINEAR
        ),
        ROCKET(
            750f,
            15,
            -1,
            true,
            R.mipmap.projectile_2,
            R.raw.game_rocket_travel,
            R.raw.game_rocket_explode,
            Behavior.HOMING
        ),
        BIG_ROCKET(
            550f,
            20,
            -1,
            true,
            R.mipmap.projectile_3,
            R.raw.game_rocket_travel,
            R.raw.game_rocket_explode,
            Behavior.HOMING
        ),
        HITSCAN(
            -1,
            20,
            -1,
            true,
            R.mipmap.projectile_1, // image has no effect
            -1,
            -1,
            Behavior.HITSCAN
        ),
        TACK(
            500,
            8,
            120,
            false,
            R.mipmap.projectile_1,
            -1,
            -1,
            Behavior.LINEAR
        ),
        BEAK(
            750f,
            2,
            true,
            R.mipmap.projectile_4,
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
        final Behavior behavior;
        final int range;

        Type(float speed, int damage, int range, boolean armorPiercing, int imageID,
            int travelSoundID,
            int impactSoundID, Behavior behavior) {
            this.speed = speed;
            this.damage = damage;
            this.range = range;
            this.armorPiercing = armorPiercing;
            this.imageID = imageID;
            this.travelSoundID = travelSoundID;
            this.impactSoundID = impactSoundID;
            this.behavior = behavior;
        }

        public boolean isArmorPiercing() {
            return armorPiercing;
        }
    }

    private static final int IMAGE_ANGLE = 90;

    private final BasicSoundPlayer audioTravel;
    private final BasicSoundPlayer audioImpact;

    public final Type type;
    final private Tower parentTower;
    private final Enemy target;
    private float velX;
    private float velY;
    public boolean remove;
    private final PointF initialLocation;

    private final float speedModifier;
    private final float damageModifier;
    private final float rangeModifier;

    public Projectile(
        Context context,
        Tower parentTower,
        PointF location,
        Type type,
        Enemy target,
        float angle,
        float speedModifier,
        float damageModifier,
        float rangeModifier) {
        super(context, location, type.imageID);

        this.type = type;
        this.parentTower = parentTower;
        this.target = target;
        this.remove = false;
        this.speedModifier = speedModifier;
        this.damageModifier = damageModifier;
        this.rangeModifier = rangeModifier;

        this.velX = (float) (getEffectiveSpeed() * Math.cos(Math.toRadians(angle)));
        this.velY = (float) (getEffectiveSpeed() * Math.sin(Math.toRadians(angle)));
        this.initialLocation = new PointF(location.x, location.y);

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
        if (!isInRange()) {
            this.remove(game.getContext());
            return;
        }

        switch (this.type.behavior) {
            case HOMING: {
                if (this.target.isAlive()) {
                    double angle = Util
                        .getAngleBetweenPoints(this.location,
                            this.target.getLocation()
                        );

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
                    this.handleKillCount(target);
                }

                remove(game.getContext());
                break;
            }
            default: {
                return;
            }
        }

        handleCollision(game.getContext(), game.getEnemies());

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

    /**
     * Checks to see if there is collision with enemies If there is, handle the collision
     *
     * @param context the projectile instance
     * @param enemies the enemies
     */
    private void handleCollision(Context context, List<Enemy> enemies) {
        for (Enemy e : enemies) {
            if (e.collides(location.x, location.y,
                bitmap.getWidth() * hitboxScaleX,
                bitmap.getHeight() * hitboxScaleY)
            ) {
                e.hitByProjectile(this);
                this.handleKillCount(e);
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

    public float getEffectiveRange() {
        return this.type.range == -1 ? -1 : this.type.range * this.rangeModifier;
    }

    private boolean isInRange() {
        float range = this.getEffectiveRange();

        return range == -1 || Math.hypot(
            this.location.x - this.initialLocation.x,
            this.location.y - this.initialLocation.y
        ) <= range;
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

    private void handleKillCount(Enemy enemy){
        if (!enemy.isAlive() && !enemy.getHasBeenKilled()){
            this.parentTower.incrementKillCount();
            enemy.setHasBeenKilled(true);
        }
    }
}
