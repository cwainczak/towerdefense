package com.wsu.towerdefense.tower;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;

import com.wsu.towerdefense.AbstractMapObject;
import com.wsu.towerdefense.Enemy;
import com.wsu.towerdefense.Game;
import com.wsu.towerdefense.Projectile;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tower extends AbstractMapObject implements Serializable {

    public enum Type {

        BASIC_LINEAR(R.mipmap.tower_1_turret, 384, 1, 1, 1, false, Projectile.Type.LINEAR, 100),
        BASIC_HOMING(R.mipmap.tower_2_turret, 384, 2, 1, 1, false, Projectile.Type.HOMING, 150),
        DOUBLE_LINEAR(R.mipmap. tower_3_turret, 384, 1.25f, 1, 1, false, Projectile.Type.LINEAR, 175),
        BIG_HOMING(R.mipmap.tower_4_turret, 384, 2, 1, 1, false, Projectile.Type.BIG_HOMING, 200);

        final int towerResID;
        public final int range;
        public final float fireRate;
        public final float projectileSpeed;
        public final float projectileDamage;
        public final boolean canSeeInvisible;
        public final Projectile.Type projectileType;
        public final int cost;

        Type(int towerResID, int range, float fireRate, float projectileSpeed, int projectileDamage,
             boolean canSeeInvisible, Projectile.Type projectileType, int cost) {
            this.towerResID = towerResID;
            this.range = range;
            this.fireRate = fireRate;
            this.projectileSpeed = projectileSpeed;
            this.projectileDamage = projectileDamage;
            this.canSeeInvisible = canSeeInvisible;
            this.projectileType = projectileType;
            this.cost = cost;
        }
    }

    public static final float BASE_SIZE = 130 * 0.875f;
    private transient Enemy target;   // The Enemy this Tower will shoot at
    private final Type type;
    private transient List<Projectile> projectiles;   // A list of the locations of projectiles shot by this Tower
    private transient double timeSinceShot = 0.0;

    private final TowerStats stats;

    private transient float angle = 0;
    private int sellPrice;
    private final List<PointF> projectileSpawnPoints;
    private boolean fireRight = false;

    /**
     * A Tower is a stationary Map object. Towers will target an Enemy that enters their range,
     * dealing damage to the Enemy until it either dies or moves out of range. Projectiles shot by a
     * Tower will track the Enemy they were shot at even if the Enemy is no longer in the Tower's
     * range.
     *
     * @param location A PointF representing the location of the towerBitmap's center
     */
    public Tower(Context context, PointF location, Type tt) {
        super(context, location, R.mipmap.tower_base);
        this.type = tt;
        this.projectiles = new ArrayList<>();
        this.stats = new TowerStats(context, tt);

        this.projectileSpawnPoints = new ArrayList<>();
        setProjectileSpawnPoints();
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

        if (target != null) {
            angle = (float) Util.getAngleBetweenPoints(location, target.getLocation());
        }

        // Calculate change in time since last projectile was fired
        timeSinceShot += delta;

        // Shoot projectile(s) if there is a target and enough time has passed
        if (target != null && timeSinceShot >= stats.getFireRate()) {

            // Rotate the projectile spawn point(s) to align with the target
            rotateProjectileSpawnPoints();

            addProjectiles(game.getContext());

            timeSinceShot = 0;
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

    public void drawLine(Canvas canvas, Paint paint) {
        if (target != null) {
            paint.reset();
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(7);
            canvas.drawLine(location.x, location.y, target.getLocation().x, target.getLocation().y,
                paint);
        }
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

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        this.projectiles = new ArrayList<>();
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
        int offset = 0;
        return loc.x - width / 2 <= this.location.x + BASE_SIZE / 2f &&
            loc.x + width / 2 >= this.location.x - BASE_SIZE / 2f &&
            loc.y - height / 2 <= this.location.y + BASE_SIZE / 2f &&
            loc.y + height / 2 >= this.location.y - BASE_SIZE / 2f;
    }

    private PointF getCenterSpawnPoint() {
        if (projectileSpawnPoints.size() > 0) {
            float x = 0, y = 0;
            for (PointF p : projectileSpawnPoints) {
                x += p.x;
                y += p.y;
            }
            x /= projectileSpawnPoints.size();
            y /= projectileSpawnPoints.size();
            return new PointF(x, y);
        }
        return null;
    }

    private void addProjectiles(Context context) {
        switch (type) {
            case BIG_HOMING:
            case BASIC_LINEAR:
                projectiles.add(
                    new Projectile(context,
                            new PointF(projectileSpawnPoints.get(0).x, projectileSpawnPoints.get(0).y),
                            stats.getProjectileType(),
                            target,
                            stats.getProjectileSpeed(),
                            stats.getProjectileDamage()
                    ));
                break;
            case BASIC_HOMING:
                int index = fireRight ? 1 : 0;
                projectiles.add(
                        new Projectile(context,
                                new PointF(projectileSpawnPoints.get(index).x, projectileSpawnPoints.get(index).y),
                                stats.getProjectileType(),
                                target,
                                stats.getProjectileSpeed(),
                                stats.getProjectileDamage()
                        ));
                fireRight = !fireRight;
                break;
            case DOUBLE_LINEAR:
                for (PointF point : projectileSpawnPoints) {
                    Projectile p = new Projectile(context,
                            new PointF(point.x, point.y),
                            stats.getProjectileType(),
                            target,
                            stats.getProjectileSpeed(),
                            stats.getProjectileDamage()
                    );
                }
            default:
                break;
        }
    }

    private void setProjectileSpawnPoints() {
        switch (type) {
            case BASIC_LINEAR:
                projectileSpawnPoints.add(new PointF(location.x,location.y - 89));
                break;
            case BASIC_HOMING:
                projectileSpawnPoints.add(new PointF(location.x - 20, location.y - 36));
                projectileSpawnPoints.add(new PointF(location.x + 17, location.y - 36));
                break;
            case DOUBLE_LINEAR:
                projectileSpawnPoints.add(new PointF(location.x - 16, location.y - 90));
                projectileSpawnPoints.add(new PointF(location.x + 14,location.y - 90));
                break;
            case BIG_HOMING:
                projectileSpawnPoints.add(new PointF(location.x,location.y - 42));
                break;
            default:
                break;
        }
    }

    private void rotateProjectileSpawnPoints() {
        if (projectileSpawnPoints.size() > 0) {
            PointF centerSpawnPoint = getCenterSpawnPoint();

            // Get angle from center spawn point to target
            double spawnAngle = Util.getAngleBetweenPoints(location, centerSpawnPoint);
            double rotAngle = Math.toRadians(angle - spawnAngle);

            for (PointF p : projectileSpawnPoints) {
                float rotX = (float) (Math.cos(rotAngle) * (p.x - location.x) - Math.sin(rotAngle) * (p.y - location.y) + location.x);
                float rotY = (float) (Math.sin(rotAngle) * (p.x - location.x) + Math.cos(rotAngle) * (p.y - location.y) + location.y);
                p.set(rotX, rotY);
            }
        }
    }

    private PointF getFirePoint(PointF spawnPoint) {
        PointF firePoint = new PointF(target.getLocation().x, target.getLocation().y);

        // Get angle from spawn point to target
        double angleToTarget = Util.getAngleBetweenPoints(location, spawnPoint);
        double angleToRotate = Math.toRadians(angle - (90 - angleToTarget));

        // Rotate firePoint around tower location by the angle
        float rotX = (float) (Math.cos(angleToRotate) * (firePoint.x - location.x) -
                Math.sin(angleToRotate) * (firePoint.y - location.y) + location.x);
        float rotY = (float) (Math.sin(angleToRotate) * (firePoint.x - location.x) +
                Math.cos(angleToRotate) * (firePoint.y - location.y) + location.y);
        firePoint.set(rotX, rotY);

        return firePoint;
    }
}
