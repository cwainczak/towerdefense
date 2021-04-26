package com.wsu.towerdefense.Model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import com.wsu.towerdefense.AbstractMapObject;
import com.wsu.towerdefense.audio.BasicSoundPlayer;
import com.wsu.towerdefense.audio.SoundSource;
import com.wsu.towerdefense.Model.tower.Tower;
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
        HITSCAN,
        STATIC
    }

    public enum Type {
        BALL(
            R.mipmap.projectile_ball,
            Behavior.LINEAR,
            1000f,
            10,
            2
        ),

        ROCKET(
            R.mipmap.projectile_rocket,
            Behavior.HOMING,
            750f,
            15,
            1
        ) {{
            piercing();
            sound(R.raw.game_rocket_travel, R.raw.game_rocket_explode);
        }},

        BIG_ROCKET(
            R.mipmap.projectile_big_rocket,
            Behavior.HOMING,
            550f,
            40,
            1
        ) {{
            piercing();
            sound(R.raw.game_rocket_travel, R.raw.game_rocket_explode);
        }},

        HITSCAN(
            R.mipmap.projectile_ball, // image has no effect
            Behavior.HITSCAN,
            -1,
            50,
            1
        ) {{
            piercing();
        }},

        TACK(
            R.mipmap.projectile_ball,
            Behavior.LINEAR,
            500,
            8,
            1
        ) {{
            range(120);
        }},

        BEAK(
            R.mipmap.projectile_beak,
            Behavior.LINEAR,
            750f,
            5,
            3
        ) {{
            piercing();
        }},

        SNOWFLAKE(
            R.mipmap.projectile_snowflake,
            Behavior.LINEAR,
            1000,
            2,
            1
        ) {{
            slow(2.0, 0.5);
        }},

        SPIKE(
                R.mipmap.projectile_spike,
                Behavior.STATIC,
                1000,
                5,
                5
        ) {{
            piercing();
        }};

        public final int imageID;
        public final Behavior behavior;
        public final float speed;
        public final int damage;
        public int pierce;
        public int range;
        public boolean armorPiercing;
        public double slowEnemyTime;
        public double slowRate;
        public int travelSoundID;
        public int impactSoundID;

        Type(
            int imageID,
            Behavior behavior,
            float speed,
            int damage,
            int pierce
        ) {
            this.imageID = imageID;
            this.behavior = behavior;
            this.speed = speed;
            this.damage = damage;
            this.pierce = pierce;

            this.range = -1;
            this.armorPiercing = false;
            this.slowEnemyTime = 0.0;
            this.slowRate = 1.0;
            this.travelSoundID = -1;
            this.impactSoundID = -1;
        }

        public void range(int range) {
            this.range = range;
        }

        public void piercing() {
            this.armorPiercing = true;
        }

        public void slow(double slowEnemyTime, double slowRate) {
            this.slowEnemyTime = slowEnemyTime;
            this.slowRate = slowRate;
        }

        public void sound(int travelSoundID, int impactSoundID) {
            this.travelSoundID = travelSoundID;
            this.impactSoundID = impactSoundID;
        }
    }


    private static final int IMAGE_ANGLE = 90;
    private final double TIME_BETWEEN_HITS = 0.12;

    private final BasicSoundPlayer audioTravel;
    private final BasicSoundPlayer audioImpact;

    public final Type type;
    final private Tower parentTower;
    private final Enemy target;
    private final PointF initTargetLocation;
    private float velX;
    private float velY;
    private boolean isActive = true;
    public boolean remove;
    private final PointF initialLocation;
    private int hits = 0;
    private double timeSinceHit = 0;

    private final float speedModifier;
    private final float damageModifier;
    private final float rangeModifier;
    private final int pierceModifier;

    private final double slowTime;
    private final double slowRate;

    public Projectile(
            Context context,
            Tower parentTower,
            PointF location,
            Type type,
            Enemy target,
            float angle,
            float speedModifier,
            float damageModifier,
            float rangeModifier,
            int pierceModifier,
            double slowTime,
            double slowRate) {
        super(context, location, type.imageID);

        this.type = type;
        this.parentTower = parentTower;
        this.target = target;
        this.remove = false;
        this.speedModifier = speedModifier;
        this.damageModifier = damageModifier;
        this.rangeModifier = rangeModifier;
        this.pierceModifier = pierceModifier;
        this.slowTime = slowTime;
        this.slowRate = slowRate;
        initTargetLocation = target.getLocation();

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
                    double angle = Util.getAngleBetweenPoints(this.location,
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
            case STATIC:
                float xDir = initialLocation.x - initTargetLocation.x;
                float yDir = initialLocation.y - initTargetLocation.y;
                // moving right
                if(xDir < 0){
                    if(location.x - initTargetLocation.x > 0){
                        this.velX = 0;
                    }
                } else{
                    if(location.x - initTargetLocation.x < 0){
                        this.velX = 0;
                    }
                }
                //moving down
                if(yDir < 0){
                    if(location.y - initTargetLocation.y > 0){
                        this.velY = 0;
                    }
                } else {
                    if(location.y - initTargetLocation.y < 0){
                        this.velY = 0;
                    }
                }
                this.location.offset((float) (this.velX * delta), (float) (this.velY * delta));
                break;
            default: {
                return;
            }
        }

        updateActive(delta);
        handleCollision(game.getContext(), checkCollision(game.getEnemies()));

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

    private Enemy checkCollision(List<Enemy> enemies) {
        for (Enemy e : enemies) {
            if (e.collides(location.x, location.y,
                bitmap.getWidth() * hitboxScaleX,
                bitmap.getHeight() * hitboxScaleY)
            ) {
                return(e);
            }
        }
        return null;
    }

    private void handleCollision(Context context, Enemy e){
        if(e != null && isActive){
            e.hitByProjectile(this);
            handleKillCount(e);
            if(++hits >= getEffectivePierce()){
                remove(context);
            } else {
                isActive = false;
                timeSinceHit = 0;
            }
        }
    }

    private void updateActive(double delta) {
        timeSinceHit += delta;

        if(timeSinceHit > TIME_BETWEEN_HITS) {
            isActive = true;
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

    public int getEffectivePierce() {
        return this.type.pierce + this.pierceModifier;
    }

    public double getSlowTime() {
        return slowTime;
    }

    public double getSlowRate() {
        return slowRate;
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

    private void handleKillCount(Enemy enemy) {
        if (!enemy.isAlive() && !enemy.getHasBeenKilled()) {
            this.parentTower.incrementKillCount();
            enemy.setHasBeenKilled(true);
        }
    }
}
