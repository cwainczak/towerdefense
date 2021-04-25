package com.wsu.towerdefense.Model.tower;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import com.wsu.towerdefense.AbstractMapObject;
import com.wsu.towerdefense.Model.Enemy;
import com.wsu.towerdefense.Model.Projectile;
import com.wsu.towerdefense.audio.AdvancedSoundPlayer;
import com.wsu.towerdefense.audio.SoundSource;
import com.wsu.towerdefense.Model.Game;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;
import com.wsu.towerdefense.Util;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tower extends AbstractMapObject implements Serializable, SoundSource {

    public enum Behavior {
        ALL,
        CYCLING
        // BOUNCE
        // RANDOM
    }

    public enum Type {
        BASIC_HOMING(
            "Homing",
            R.mipmap.tower_basic_homing_comb,
            R.mipmap.tower_basic_homing,
            384,
            2,
            Projectile.Type.ROCKET,
            300,
            -1,
            false,
            true,
            Behavior.CYCLING,
            new PointF[]{
                new PointF(-20, -36),
                new PointF(17, -36)
            }
        ),
        BASIC_LINEAR(
            "Basic",
            R.mipmap.tower_basic_linear_comb,
            R.mipmap.tower_basic_linear,
            384,
            1,
            Projectile.Type.BALL,
            150,
            R.raw.game_tower_shoot_1,
            false,
            true,
            Behavior.ALL,
            new PointF[]{
                new PointF(0, -80)
            }
        ),
        DOUBLE_LINEAR(
            "Double",
            R.mipmap.tower_double_linear_comb,
            R.mipmap.tower_double_linear,
            384,
            1.25f,
            Projectile.Type.BALL,
            300,
            R.raw.game_tower_shoot_1,
            false,
            true,
            Behavior.ALL,
            new PointF[]{
                new PointF(-16, -80),
                new PointF(14, -80)
            }
        ),
        BIG_HOMING(
            "Big Rocket",
            R.mipmap.tower_big_homing_comb,
            R.mipmap.tower_big_homing,
            384,
            2.5f,
            Projectile.Type.BIG_ROCKET,
            400,
            -1,
            false,
            true,
            Behavior.ALL,
            new PointF[]{
                new PointF(0, -42)
            }
        ),
        SNIPER(
            "Sniper",
            R.mipmap.tower_sniper_comb,
            R.mipmap.tower_sniper,
            3000,
            3f,
            Projectile.Type.HITSCAN,
            350,
            R.raw.game_tower_shoot_1,
            true,
            true,
            Behavior.ALL,
            new PointF[]{
                new PointF(0, 0)
            }
        ),
        TACK_SHOOTER(
            "Tack Shooter",
            R.mipmap.tower_tack_comb,
            R.mipmap.tower_tack,
            250,
            0.75f,
            Projectile.Type.TACK,
            300,
            R.raw.game_tower_shoot_1,
            false,
            false,
            Behavior.ALL,
            new PointF[]{
                new PointF(65, 0),
                new PointF(65 / (float) Math.sqrt(2), -65 / (float) Math.sqrt(2)),
                new PointF(0, -65),
                new PointF(-65 / (float) Math.sqrt(2), -65 / (float) Math.sqrt(2)),
                new PointF(-65, 0),
                new PointF(-65 / (float) Math.sqrt(2), 65 / (float) Math.sqrt(2)),
                new PointF(0, 65),
                new PointF(65 / (float) Math.sqrt(2), 65 / (float) Math.sqrt(2))
            }
        ),
        NESTOR(
            "Nestor",
            R.mipmap.tower_nestor_comb,
            R.mipmap.tower_nestor,
            600,
            0.25f,
            Projectile.Type.BEAK,
            1200,
            R.raw.game_tower_shoot_1,
            false,
            true,
            Behavior.ALL,
            new PointF[]{
                new PointF(0, -50)
            }
        ),
        ICE(
            "Ice",
            R.mipmap.tower_ice_comb,
            R.mipmap.tower_ice,
            384,
            1.5f,
            Projectile.Type.SNOWFLAKE,
            100,
            R.raw.game_tower_shoot_1,
            false,
            true,
            Behavior.ALL,
            new PointF[]{
                new PointF(0, -80)
            }
        ),
        SPIKE(
                "Spike Thrower",
                R.mipmap.tower_spike_thrower_comb,
                R.mipmap.tower_spike_thrower,
                384,
                1.5f,
                Projectile.Type.SPIKE,
                350,
                R.raw.game_tower_shoot_1,
                false,
                true,
                Behavior.ALL,
                new PointF[]{
                        new PointF(0, -10)
                }
        );

        public final String name;
        public final int uiResID;
        public final int towerResID;
        public final int range;
        public final float fireRate;
        public final Projectile.Type projectileType;
        public final int cost;
        public final int shootSoundID;
        public final boolean canSeeInvisible;
        public final boolean turns;
        public final Behavior behavior;

        /*
         Each point in this list represents a projectile spawn point
         relative to the center of the turret image
         */
        public final PointF[] spawnPoints;

        Type(
            String name,
            int uiResID,
            int towerResID,
            int range,
            float fireRate,
            Projectile.Type projectileType,
            int cost,
            int shootSoundID,
            boolean canSeeInvisible,
            boolean turns,
            Behavior behavior,
            PointF[] spawnPoints) {
            this.name = name;
            this.uiResID = uiResID;
            this.towerResID = towerResID;
            this.range = range;
            this.fireRate = fireRate;
            this.projectileType = projectileType;
            this.cost = cost;
            this.shootSoundID = shootSoundID;
            this.canSeeInvisible = canSeeInvisible;
            this.turns = turns;
            this.behavior = behavior;
            this.spawnPoints = spawnPoints;
        }
    }

    private static final int IMAGE_ANGLE = 90;
    /**
     * Face north
     */
    private static final int START_ANGLE = 0;

    public static final float BASE_SIZE = 130 * 0.875f;

    private transient AdvancedSoundPlayer audioShoot;

    private int killCount = 0;
    private transient Enemy target;   // The Enemy this Tower will shoot at
    private final Type type;
    private transient List<Projectile> projectiles;   // A list of the locations of projectiles shot by this Tower
    private transient double timeSinceShot = 0.0;

    private final TowerStats stats;

    private transient float angle;
    private transient int cycle;

    /**
     * A Tower is a stationary Map object. Towers will target an Enemy that enters their range,
     * dealing damage to the Enemy until it either dies or moves out of range. Projectiles shot by a
     * Tower will track the Enemy they were shot at even if the Enemy is no longer in the Tower's
     * range.
     *
     * @param location A PointF representing the location of the towerBitmap's center
     */
    public Tower(Context context, PointF location, Type type) {
        super(context, location, R.mipmap.tower_base);
        this.type = type;
        this.projectiles = new ArrayList<>();
        this.stats = new TowerStats(context, type);

        this.angle = START_ANGLE - IMAGE_ANGLE;

        this.audioShoot = type.shootSoundID >= 0
            ? new AdvancedSoundPlayer(this.type.shootSoundID)
            : null;

        this.cycle = 0;
    }

    /**
     * Updates the Tower based on the the change in time since the Tower was last updated.
     *
     * @param game  the Game object this Tower belongs to
     * @param delta amount of time that has passed between updates
     */
    @Override
    public void update(Game game, double delta) {
        // Remove target if it goes out of range or reaches end of path
        if (distanceToEnemy(target) > stats.getRange() ||
            (target != null && target.isAtPathEnd())) {
            target = null;
        }

        // Look for new target if there is no current target
        if (target == null) {
            List<Enemy> enemies = game.getEnemies();
            for (int i = 0; i < enemies.size(); i++) {
                Enemy e = enemies.get(i);
                if (distanceToEnemy(e) < stats.getRange()) {
                    if (!e.isInvisible() || (e.isInvisible() && stats.canSeeInvisible())) {
                        target = e;

                        break; // Stop looking for a target
                    }
                }
            }
        }

        // Calculate change in time since last projectile was fired
        timeSinceShot += delta;

        if (this.type.turns && target != null) {
            angle = (float) Util.getAngleBetweenPoints(location, target.getLocation());
        }

        // Shoot another projectile if there is a target and enough time has passed
        if (target != null && timeSinceShot >= stats.getFireRate()) {
            shootProjectiles(game.getContext());

            timeSinceShot = 0;

            if (this.audioShoot != null) {
                this.audioShoot.play(game.getContext(), Settings.getSFXVolume(game.getContext()));
            }
        }

        // Update each projectile
        for (Iterator<Projectile> projectileIt = projectiles.iterator(); projectileIt.hasNext(); ) {
            Projectile p = projectileIt.next();
            p.update(game, delta);

            if (p.remove) {
                projectileIt.remove();
            }
        }

        // If the target died stop targeting it
        if (target != null && !target.isAlive()) {
            target = null;
        }
    }

    /**
     * Draws this Tower and all its objects to the provided Canvas.
     *
     * @param lerp   interpolation factor
     * @param canvas the canvas this Tower will be drawn on
     * @param paint  the paint object used to paint onto the canvas
     */
    @Override
    public void render(double lerp, Canvas canvas, Paint paint) {
        // Draw the tower base image
        canvas.drawBitmap(
            bitmap,
            location.x - bitmap.getWidth() / 2f,
            location.y - bitmap.getHeight() / 2f,
            null
        );

        // Draw the tower turret image
        Matrix matrix = new Matrix();
        matrix.postRotate(
            angle + IMAGE_ANGLE,
            stats.getTurretImage().getWidth() / 2f,
            stats.getTurretImage().getHeight() / 2f
        );
        matrix.postTranslate(
            location.x - bitmap.getWidth() / 2f,
            location.y - bitmap.getHeight() / 2f
        );
        canvas.drawBitmap(stats.getTurretImage(), matrix, null);

        // Draw each projectile
        for (Projectile p : projectiles) {
            p.render(lerp, canvas, paint);
        }
    }

    /**
     * A helper method that calculates the distance from the center of this Tower to the center of a
     * given Enemy
     *
     * @param enemy The Enemy object to calculate the distance to
     * @return A double representing the distance to the enemy
     */
    private double distanceToEnemy(Enemy enemy) {
        if (enemy == null) {
            return -1;
        }

        float a = Math.abs(location.x - enemy.getLocation().x);
        float b = Math.abs(location.y - enemy.getLocation().y);
        return Math.hypot(a, b);
    }

    /**
     * A method to determine whether or not a given hitbox collides with the Tower's hitbox.
     *
     * @param loc    The center of the hitbox to check
     * @param width  The width of the hitbox
     * @param height The height of the hitbox
     * @return true if the given hitbox overlaps the Tower's hitbox
     */
    public boolean collides(PointF loc, float width, float height) {
        return loc.x - width / 2 <= this.location.x + BASE_SIZE / 2f &&
            loc.x + width / 2 >= this.location.x - BASE_SIZE / 2f &&
            loc.y - height / 2 <= this.location.y + BASE_SIZE / 2f &&
            loc.y + height / 2 >= this.location.y - BASE_SIZE / 2f;
    }

    public TowerStats getStats() {
        return stats;
    }

    public int getCost() {
        return type.cost;
    }

    public Type getType() {
        return type;
    }

    public float getRange() {
        return this.type.range;
    }

    private void shootProjectiles(Context context) {
        switch (type.behavior) {
            case ALL: {
                for (PointF basePoint : type.spawnPoints) {
                    shootProjectile(context, basePoint);
                }
                break;
            }
            case CYCLING: {
                PointF basePoint = new PointF(
                    type.spawnPoints[cycle].x,
                    type.spawnPoints[cycle].y
                );
                shootProjectile(context, basePoint);

                cycle = (cycle + 1) % type.spawnPoints.length;
                break;
            }
            default:
                break;
        }
    }

    private void shootProjectile(Context context, PointF basePoint) {
        PointF rotatedPoint = Util.rotatePoint(basePoint, angle + IMAGE_ANGLE);
        PointF spawnPoint = new PointF(
            this.location.x + rotatedPoint.x,
            this.location.y + rotatedPoint.y
        );
        double angle = Util.getAngleBetweenPoints(this.location, spawnPoint);

        projectiles.add(
            new Projectile(context, this,
                spawnPoint,
                stats.getProjectileType(),
                target,
                (float) angle,
                stats.getProjectileSpeed(),
                stats.getProjectileDamage(),
                stats.getProjectileRange(),
                stats.getProjectilePierce(),
                stats.getProjectileSlowTime(),
                stats.getProjectileSlowRate()
            ));
    }

    @Override
    public void release() {
        if (this.audioShoot != null) {
            this.audioShoot.release();
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        this.projectiles = new ArrayList<>();

        this.angle = START_ANGLE - IMAGE_ANGLE;

        this.audioShoot = type.shootSoundID >= 0
            ? new AdvancedSoundPlayer(this.type.shootSoundID)
            : null;
    }

    public int getKillCount() {
        return killCount;
    }

    public void incrementKillCount() {
        this.killCount++;
    }

}
