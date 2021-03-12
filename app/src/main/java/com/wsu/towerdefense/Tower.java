package com.wsu.towerdefense;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tower extends AbstractMapObject implements Serializable {

    private final int radius;
    private transient Enemy target;   // The Enemy this Tower will shoot at

    private final int projectileResourceID;
    private transient List<Projectile> projectiles;   // A list of the locations of projectiles shot by this Tower
    private final double fireRate = 1; // Time (in seconds) between firing of projectiles
    private double timeSinceShot = 0.0;
    private final int damage;
    private final float projectileVelocity;

    /**
     * The monetary cost of the tower
     */
    public final int cost;

    /**
     * A Tower is a stationary Map object. Towers will target an Enemy that enters their range,
     * dealing damage to the Enemy until it either dies or moves out of range. Projectiles shot by a
     * Tower will track the Enemy they were shot at even if the Enemy is no longer in the Tower's
     * range.
     *  @param location           A PointF representing the location of the towerBitmap's center
     * @param radius             The radius of this Tower's detection range
     * @param projectileVelocity The velocity of this Tower's Projectiles
     * @param damage             The amount of damage each projectile from this Tower deals to an
     * @param cost               The amount of money required to purchase this Tower
     */
    public Tower(PointF location, int radius, float projectileVelocity, int damage, int cost) {
        super(location, R.mipmap.tower);
        this.radius = radius;
        this.projectileResourceID = R.mipmap.projectile;
        this.projectileVelocity = projectileVelocity;
        this.damage = damage;
        this.cost = cost;
        this.projectiles = new ArrayList<>();
    }

    /**
     * Updates the Tower based on the the change in time since the Tower was last updated.
     *
     * @param game  the Game object this Tower belongs to
     * @param delta amount of time that has passed between updates
     */
    @Override
    protected void update(Game game, double delta) {
        // Remove target if it goes out of range
        if (distanceToEnemy(target) > radius) {
            target = null;
        }

        // Look for new target if there is no current target
        if (target == null) {
            List<Enemy> enemies = game.getEnemies();
            for (int i = 0; i < enemies.size(); i++) {
                if (distanceToEnemy(enemies.get(i)) < radius) {
                    target = enemies.get(i);

                    break; // Stop looking for a target
                }
            }
        }

        // Calculate change in time since last projectile was fired
        timeSinceShot += delta;

        // Shoot another projectile if there is a target and enough time has passed
        if (target != null && timeSinceShot >= fireRate) {
            projectiles.add(new Projectile(new PointF(location.x, location.y),
                projectileResourceID, projectileVelocity, target));
            timeSinceShot = 0;
        }

        // Update each projectile
        for (Iterator<Projectile> projectileIt = projectiles.iterator(); projectileIt.hasNext(); ) {
            Projectile p = projectileIt.next();
            p.update(game, delta);

            // If the projectile hits its target deal damage to the target
            if (p.hitTarget()) {
                p.damageTarget(damage);

                // Remove the projectile from the projectile list
                projectileIt.remove();
            } else if (!p.getTarget().isAlive()) {
                // If the current projectile did not hit its target, but a previous projectile
                // hit and killed its target remove the current projectile
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
    protected void render(double lerp, Canvas canvas, Paint paint) {
        // Draw the Tower's bitmap image
        canvas.drawBitmap(
            bitmap,
            location.x - bitmap.getWidth() / 2f,
            location.y - bitmap.getHeight() / 2f,
            null
        );

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

        float a = Math.abs(location.x - enemy.location.x);
        float b = Math.abs(location.y - enemy.location.y);
        return Math.hypot(a, b);
    }

    /**
     * A helper method that draws a circular outline representing the range of this Tower.
     *
     * @param canvas The Canvas to draw the range on.
     * @param paint  The Paint used to draw the range.
     */
    public void drawRange(Canvas canvas, Paint paint) {
        float width = paint.getStrokeWidth();
        paint.setStrokeWidth(width + 6);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAlpha(150);

        canvas.drawCircle(location.x, location.y, radius, paint);

        paint.setAlpha(255);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.FILL);
    }

    public void drawLine(Canvas canvas, Paint paint) {
        if (target != null) {
            float width = paint.getStrokeWidth();
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(width + 6);
            canvas.drawLine(location.x, location.y, target.location.x, target.location.y, paint);
            paint.setStrokeWidth(width);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        this.projectiles = new ArrayList<>();
    }
}
