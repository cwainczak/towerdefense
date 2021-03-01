package com.wsu.towerdefense;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A Tower is a stationary Map object. Towers will target an Enemy that enters their range,
 * dealing damage to the Enemy until it either dies or moves out of range.
 */
public class Tower extends AbstractMapObject {

    private int radius;
    private Enemy target;   // The Enemy this Tower will shoot at

    private Bitmap projectileBmp;
    private List<Projectile> projectiles;   // A list of the locations of projectiles shot by this Tower
    private double fireRate = 1; // Time (in seconds) between firing of projectiles
    private double timeSinceShot = 0.0;
    private int damage;
    private float projectileVelocity = 1000;

    /**
     * @param location         A PointF representing the location of the towerBitmap's center
     * @param towerBitmap      A bitmap image of this Tower object
     * @param radius           The radius of this Tower's detection range
     * @param projectileBitmap A bitmap image of the projectiles shot by this Tower
     */
    public Tower(PointF location, Bitmap towerBitmap, int radius, Bitmap projectileBitmap, int damage) {
        super(location, towerBitmap);
        this.radius = radius;
        this.projectileBmp = projectileBitmap;
        this.damage = damage;
        this.projectiles = new ArrayList<>();
    }

    /**
     * Updates the Tower based on the the change in time
     * since the Tower was last updated.
     *
     * @param game  the Game object this Tower belongs to
     * @param delta amount of time that has passed between updates
     */
    @Override
    protected void update(Game game, double delta) {
        // Remove target if it goes out of range
        if (distanceToEnemy(target) > radius) target = null;

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
                    projectileVelocity, projectileBmp, target));
            timeSinceShot = 0;
        }

        // Update the position of each projectile
        for (Iterator<Projectile> projectileIt = projectiles.iterator(); projectileIt.hasNext(); ) {
            Projectile p = projectileIt.next();
            p.update(game, delta);

            // If the projectile hits the target deal damage to the target
            if (p.hitTarget()) {
                p.damageTarget(damage);

                // If p's target died remove all projectiles that were targeting the same Enemy
                if (!p.target.isAlive) {
                    // Use a local copy of current projectile's target. This is needed in case
                    // the Tower is currently targeting a different Enemy or if the Enemy the
                    // Tower was targeting moved out of range after the projectile was shot.
                    Enemy e = p.target;
                    Iterator<Projectile> innerProjectileIt = projectiles.iterator();
                    while (innerProjectileIt.hasNext()) {
                        Projectile innerP = innerProjectileIt.next();

                        // Remove projectiles that target the now dead Enemy
                        if (innerP.target.equals(e)) {
                            innerProjectileIt.remove();
                        }
                    }
                    target = null;
                } else {
                    // Remove only the current projectile
                    projectileIt.remove();
                }
            }
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
        canvas.drawBitmap(bitmap, location.x - bitmap.getWidth() / 2, location.y - bitmap.getHeight() / 2,
                null);

        // Draw each projectile
        for (Projectile p : projectiles) {
            p.render(lerp, canvas, paint);
        }

        // FOR TESTING / DEBUG

        // Draw the Tower's range
        drawRange(canvas, paint);

        // Draw a line to the target Enemy, interpolating Enemy position
        if (target != null) {
            float width = paint.getStrokeWidth();
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(width + 6);
            canvas.drawLine(location.x, location.y,
                    (float) (target.location.x + target.getVelocityX() * lerp),
                    (float) (target.location.y + target.getVelocityY() * lerp), paint);
            paint.setStrokeWidth(width);
        }

    }

    /**
     * A helper method that calculates the distance from the center of this Tower to
     * the center of a given Enemy
     *
     * @param enemy The Enemy object to calculate the distance to
     * @return A double representing the distance to the enemy
     */
    private double distanceToEnemy(Enemy enemy) {
        if (enemy == null) return -1;

        float a = Math.abs(location.x - enemy.location.x);
        float b = Math.abs(location.y - enemy.location.y);
        return Math.hypot(a, b);
    }

    /**
     * A helper method that draws a circular outline representing the range
     * of this Tower.
     *
     * @param canvas The Canvas to draw the range on.
     * @param paint  The Paint used to draw the range.
     */
    private void drawRange(Canvas canvas, Paint paint) {
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

    public void setTarget(Enemy target) {
        this.target = target;
    }

    public Enemy getTarget() {
        return target;
    }
}
