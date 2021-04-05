package com.wsu.towerdefense.tower;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import com.wsu.towerdefense.AbstractMapObject;
import com.wsu.towerdefense.Enemy;
import com.wsu.towerdefense.Game;
import com.wsu.towerdefense.Projectile;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Util;
import com.wsu.towerdefense.audio.AdvancedSoundPlayer;
import com.wsu.towerdefense.audio.SoundSource;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tower extends AbstractMapObject implements Serializable, SoundSource {

    public enum Type {
        BASIC_HOMING(
            R.mipmap.tower_2_turret,
            384,
            2,
            1,
            1,
            Projectile.Type.HOMING,
            150,
            -1
        ),
        BASIC_LINEAR(
            R.mipmap.tower_1_turret,
            384,
            1,
            1,
            1,
            Projectile.Type.LINEAR,
            100,
            R.raw.game_tower_shoot_1
        );

        final int towerResID;
        public final int range;
        public final float fireRate;
        public final float projectiveSpeed;
        public final float projectileDamage;
        public final Projectile.Type projectileType;
        public final int cost;
        public final int shootSoundID;

        Type(
            int towerResID,
            int range,
            float fireRate,
            float projectiveSpeed,
            float projectileDamage,
            Projectile.Type projectileType,
            int cost,
            int shootSoundID
        ) {
            this.towerResID = towerResID;
            this.range = range;
            this.fireRate = fireRate;
            this.projectiveSpeed = projectiveSpeed;
            this.projectileDamage = projectileDamage;
            this.projectileType = projectileType;
            this.cost = cost;
            this.shootSoundID = shootSoundID;
        }
    }

    public static final float BASE_SIZE = 130 * 0.875f;

    private transient AdvancedSoundPlayer audioShoot;

    private transient Enemy target;   // The Enemy this Tower will shoot at
    private final Type type;
    private transient List<Projectile> projectiles;   // A list of the locations of projectiles shot by this Tower
    private transient double timeSinceShot = 0.0;

    private final TowerStats stats;

    private transient float angle = 0;

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

        this.audioShoot = type.shootSoundID >= 0
            ? new AdvancedSoundPlayer(this.type.shootSoundID)
            : null;
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
                if (distanceToEnemy(enemies.get(i)) < stats.getRange()) {
                    target = enemies.get(i);

                    break; // Stop looking for a target
                }
            }
        }

        // Calculate change in time since last projectile was fired
        timeSinceShot += delta;

        // Shoot another projectile if there is a target and enough time has passed
        if (target != null && timeSinceShot >= stats.getFireRate()) {
            projectiles.add(
                new Projectile(game.getContext(),
                    new PointF(location.x, location.y),
                    stats.getProjectileType(),
                    target,
                    stats.getProjectileSpeed(),
                    stats.getProjectileDamage()
                )
            );
            timeSinceShot = 0;

            if (this.audioShoot != null) {
                this.audioShoot.play(game.getContext());
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

        if (target != null) {
            angle = (float) Util.getAngleBetweenPoints(location, target.getLocation()) + 90;
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
            angle,
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
        this.audioShoot = type.shootSoundID >= 0
            ? new AdvancedSoundPlayer(type.shootSoundID)
            : null;
    }
}
