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

    public enum Type {

        BASIC_HOMING(384, 750f, 2, 150),
        BASIC_LINEAR(384, 1000f, 1, 100);

        final int radius;
        final float projectiveVelocity;
        final double fireRate;
        public final int cost;
        final int projectileResID;


        Type(int someRadius, float someProjectiveVelocity, double someFireRate, int someCost){
            this.radius = someRadius;
            this.projectiveVelocity = someProjectiveVelocity;
            this.fireRate = someFireRate;
            this.cost = someCost;
            this.projectileResID = R.mipmap.projectile;
        }

    }

    private transient Enemy target;   // The Enemy this Tower will shoot at
    private final Type type;
    private transient List<Projectile> projectiles;   // A list of the locations of projectiles shot by this Tower
    private double timeSinceShot = 0.0;
    private Projectile.Type projectileType;

    /**
     * A Tower is a stationary Map object. Towers will target an Enemy that enters their range,
     * dealing damage to the Enemy until it either dies or moves out of range. Projectiles shot by a
     * Tower will track the Enemy they were shot at even if the Enemy is no longer in the Tower's
     * range.
     *
     *  @param location           A PointF representing the location of the towerBitmap's center
     */
    public Tower(PointF location, Type tt) {
        super(location, R.mipmap.tower);
        this.type = tt;

        if (tt == Type.BASIC_LINEAR) {
            projectileType = Projectile.Type.LINEAR;
        } else if (tt == Type.BASIC_HOMING) {
            projectileType = Projectile.Type.HOMING;
        } else {
            projectileType = null;
        }

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
        // Remove target if it goes out of range or reaches end of path
        if (distanceToEnemy(target) > this.type.radius ||
                (target != null && target.isAtPathEnd())) {
            target = null;
        }

        // Look for new target if there is no current target
        if (target == null) {
            List<Enemy> enemies = game.getEnemies();
            for (int i = 0; i < enemies.size(); i++) {
                if (distanceToEnemy(enemies.get(i)) < this.type.radius) {
                    target = enemies.get(i);

                    break; // Stop looking for a target
                }
            }
        }

        // Calculate change in time since last projectile was fired
        timeSinceShot += delta;

        // Shoot another projectile if there is a target and enough time has passed
        if (target != null && timeSinceShot >= this.type.fireRate) {
            projectiles.add(new Projectile(new PointF(location.x, location.y), projectileType, target));
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

    public void drawLine(Canvas canvas, Paint paint) {
        if (target != null) {
            paint.reset();
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(7);
            canvas.drawLine(location.x, location.y, target.location.x, target.location.y, paint);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        this.projectiles = new ArrayList<>();
    }

    public float getRange() {
        return this.type.radius;
    }

    public int getCost(){
        return this.type.cost;
    }

    public Type getType() {
        return type;
    }

}
