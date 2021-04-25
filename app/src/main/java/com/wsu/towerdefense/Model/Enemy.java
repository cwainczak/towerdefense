package com.wsu.towerdefense.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import com.wsu.towerdefense.AbstractMapObject;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Util;
import java.util.List;
import java.util.ListIterator;

public class Enemy extends AbstractMapObject {

    public enum Type {
        // Standard enemy types
        S1(200, 20, 14, 1, false, R.mipmap.standard_slime_1, -1),
        S2(250, 30, 18, 2, false, R.mipmap.standard_slime_2, -1),
        S3(350, 30 , 25, 3, false, R.mipmap.standard_slime_3, -1),

        // Armored enemy types
        A1(100, 40, 20, 1, false, R.mipmap.armored_slime_1, R.mipmap.armor_1),
        A2(100, 150, 25, 2, false, R.mipmap.armored_slime_2, R.mipmap.armor_2),
        A3(50, 300, 40, 3, false, R.mipmap.armored_slime_3, R.mipmap.armor_3),

        // Invisible enemy types
        I1(200, 20, 15, 1, true, R.mipmap.invisible_slime_1, -1),
        I2(300, 20, 20, 2, true, R.mipmap.invisible_slime_2, -1),
        I3(200, 50, 35, 3, true, R.mipmap.invisible_slime_3, -1);
        
        final float speed;
        final int hp;
        final int price;
        final int damage;
        final boolean invisible;
        final int resource;
        final int armorResource;

        Type(float speed, int hp, int price, int damage, boolean invisible, int resource,
            int armorResource) {
            this.speed = speed;
            this.hp = hp;
            this.price = price;
            this.damage = damage;
            this.invisible = invisible;
            this.resource = resource;
            this.armorResource = armorResource;
        }

        public int getDamage() {
            return this.damage;
        }
    }

    private static final float HEALTH_BAR_Y_OFFSET = -70;
    private static final float HEALTH_BAR_WIDTH = 90;
    private static final float HEALTH_BAR_HEIGHT = 15;
    private static final int HEALTH_BAR_BG_COLOR = Color.RED;
    private static final int HEALTH_BAR_FG_COLOR = Color.GREEN;

    private final Type type;

    private int hp;
    private boolean isAlive;
    private boolean hasBeenKilled = false;
    private final boolean isInvisible;

    private float velX;
    private float velY;

    private final ListIterator<PointF> path;
    private boolean isAtPathEnd;
    private PointF target;

    private Bitmap armor;

    private double slowTime = 0.0;
    private float speed;

    /**
     * An Enemy is a movable Map object. Enemies will move along a predetermined path defined by the
     * Map they are placed on. They will continue moving along the path until they reach the end or
     * are killed by a Projectile.
     *
     * @param path A List of Points for the current location to move towards
     * @param type enum containing information which will be consistent across all enemies of the
     *             same type (speed, hp, price, resource)
     */
    public Enemy(Context context, Type type, List<PointF> path) {
        super(context, path.get(0), type.resource);

        this.type = type;
        this.path = path.listIterator();
        this.hp = type.hp;

        this.target = this.path.next();
        this.isAlive = true;
        this.isInvisible = type.invisible;
        this.isAtPathEnd = false;
        this.velX = 0;
        this.velY = 0;

        this.armor = (type.armorResource == -1) ? null : Util.getBitmapByID(context, type.armorResource);
        this.speed = type.speed;
    }

    /**
     * Updates the Enemy's location based on the Enemy's velocity and the change in time since the
     * location was last updated.
     *
     * @param game  the Game object this Enemy belongs to
     * @param delta amount of time that has passed between updates
     */
    @Override
    public void update(Game game, double delta) {
        // Moves this Enemy to its next location using velocity and delta. If enemy will be at or past
        // target, and there are more Points in path, set location to target instead and set target to
        // next Point in path.

        // Update the time left for this Enemy to be slowed
        if (slowTime > 0) {
            if (slowTime > delta) {
                slowTime -= delta;
            } else {
                slowTime = 0.0;
                speed = type.speed;
                updateVelocity();
            }
        }

        //check if distance between location and target is less than or equal to distance between
        //location and next location, and there are more Points int path
        double distance = Math.hypot(location.x - target.x, location.y - target.y);

        if (distance <= Math.abs(speed * delta)) {
            if (path.hasNext()) {
                //set location to target, and update target
                location = new PointF(target.x, target.y);
                target = path.next();

                updateVelocity();
            } else {
                // If there are no more points in the path
                isAtPathEnd = true;
            }
        }

        // get new location based on velocity values and current location
        this.location = Util.getNewLoc(this.location, this.velX, this.velY, delta);
    }

    /**
     * Draws this Enemy's Bitmap image to the provided Canvas, interpolating changes in position to
     * maintain smooth movement regardless of updates since last drawn.
     *
     * @param lerp   interpolation factor
     * @param canvas the canvas this Enemy will be drawn on
     * @param paint  the paint object used to paint onto the canvas
     */
    @Override
    public void render(double lerp, Canvas canvas, Paint paint) {
        if (hp > 0) {
            float x = (float) Math.round(location.x + velX * lerp);
            float y = (float) Math.round(location.y + velY * lerp);

            // Draw the Enemy bitmap image
            canvas.drawBitmap(bitmap, x - bitmap.getWidth() / 2f,
                y - bitmap.getHeight() / 2f, null);

            // Draw Enemy armor, if present
            if (armor != null) {
                canvas.drawBitmap(armor, x - bitmap.getWidth() / 2f,
                    y - bitmap.getHeight() / 2f, null);
            }

            // show health bar if damaged
            if (this.hp < this.type.hp) {
                paint.reset();

                paint.setColor(HEALTH_BAR_BG_COLOR);
                canvas.drawRect(
                    x - HEALTH_BAR_WIDTH / 2,
                    y + HEALTH_BAR_Y_OFFSET,
                    x + HEALTH_BAR_WIDTH / 2,
                    y + HEALTH_BAR_Y_OFFSET + HEALTH_BAR_HEIGHT,
                    paint
                );

                float remainingHealth = (float) this.hp / this.type.hp;
                paint.setColor(HEALTH_BAR_FG_COLOR);
                canvas.drawRect(
                    x - HEALTH_BAR_WIDTH / 2,
                    y + HEALTH_BAR_Y_OFFSET,
                    x - HEALTH_BAR_WIDTH / 2 + remainingHealth * HEALTH_BAR_WIDTH,
                    y + HEALTH_BAR_Y_OFFSET + HEALTH_BAR_HEIGHT,
                    paint
                );
            }
        }
    }

    /**
     * A method to determine whether or not a given hitbox collides with the Enemy's hitbox.
     *
     * @param x      The x coordinate of the hitbox center
     * @param y      The y coordinate of the hitbox center
     * @param width  The width of the hitbox
     * @param height The height of the hitbox
     * @return true if the given hitbox overlaps the Enemy's hitbox
     */
    public boolean collides(float x, float y, float width, float height) {
        return x - width / 2 <= this.location.x + bitmap.getWidth() / 2f &&
            x + width / 2 >= this.location.x - bitmap.getWidth() / 2f &&
            y - height / 2 <= this.location.y + bitmap.getHeight() / 2f &&
            y + height / 2 >= this.location.y - bitmap.getHeight() / 2f;
    }

    public void hitByProjectile(Projectile projectile) {
        if (projectile.type.slowRate < 1.0) {
            slow(projectile);
        }
        if (armor == null) {
            hp -= (int) projectile.getEffectiveDamage();
            if (hp <= 0) {
                isAlive = false;
            }
        } else if (projectile.type.armorPiercing) {
            armor = null;
        }
    }

    /**
     * Reduces this Enemy's speed for the time and amount specified by the projectile passed
     *
     * @param projectile The Projectile that determines how long and by how much to slow the enemy
     */
    private void slow(Projectile projectile) {
        if (projectile.getSlowTime() > slowTime) {
            slowTime = projectile.getSlowTime();
            speed = (float) (type.speed * projectile.getSlowRate());
            updateVelocity();
        }
    }

    private void updateVelocity() {
        PointF newVel = Util.velocityTowardsPoint(this.location, this.target, speed);
        this.velX = newVel.x;
        this.velY = newVel.y;
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public boolean isAtPathEnd() {
        return this.isAtPathEnd;
    }

    public int getPrice() {
        return this.type.price;
    }

    public void setVelX(float velX) {
        this.velX = velX;
    }

    public void setVelY(float velY) {
        this.velY = velY;
    }

    public PointF getTarget() {
        return target;
    }

    public Type getType() {
        return type;
    }

    public boolean isInvisible() {
        return isInvisible;
    }

    public boolean getHasBeenKilled() {
        return hasBeenKilled;
    }

    public void setHasBeenKilled(boolean hasBeenKilled) {
        this.hasBeenKilled = hasBeenKilled;
    }
}