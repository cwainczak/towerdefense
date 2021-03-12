package com.wsu.towerdefense;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import android.graphics.Typeface;
import java.util.List;
import java.util.ListIterator;

public class Enemy extends AbstractMapObject {

    /**
     * Enemy's velocity in the x direction
     */
    private float velocityX;
    /**
     * Enemy's velocity in the y direction
     */
    private float velocityY;
    /**
     * pixels per second
     */
    private final float speed;

    private final ListIterator<PointF> path;
    private PointF target;

    /**
     * Is the enemy alive
     */
    private boolean isAlive;

    /**
     * Did the enemy reach the last point in its path
     */
    private boolean isAtPathEnd;

    /**
     * Enemy's hit points
     */
    private int hp;

    /**
     * An Enemy is a movable Map object. Enemies will move along a predetermined path defined by the
     * Map they are placed on. They will continue moving along the path until they reach the end or
     * are killed by a Projectile.
     *
     * @param path  A List of Points for the current location to move towards
     * @param hp    The amount of hit points this Enemy has
     * @param speed distance enemy moves, pixels per second
     */
    public Enemy(List<PointF> path, int hp, float speed) {
        super(path.get(0), R.mipmap.enemy);

        this.path = path.listIterator();
        this.hp = hp;
        this.speed = speed;

        this.target = this.path.next();
        this.isAlive = true;
        this.isAtPathEnd = false;
        this.velocityX = 0;
        this.velocityY = 0;
    }

    /**
     * Updates the Enemy's location based on the Enemy's velocity and the change in time since the
     * location was last updated.
     *
     * @param game  the Game object this Enemy belongs to
     * @param delta amount of time that has passed between updates
     */
    @Override
    protected void update(Game game, double delta) {
        // Moves this Enemy to its next location using velocity and delta. If enemy will be at or past
        // target, and there are more Points in path, set location to target instead and set target to
        // next Point in path.

        //check if distance between location and target is less than or equal to distance between
        //location and next location, and there are more Points int path
        double distance = Math.hypot(location.x - target.x, location.y - target.y);

        if (distance <= Math.abs(speed * delta)) {
            if (path.hasNext()) {
                //set location to target, and update target
                location = new PointF(target.x, target.y);
                target = path.next();

                float dx = target.x - location.x;
                float dy = target.y - location.y;
                distance = Math.hypot(dx, dy);
                velocityX = speed * (float) (dx / distance);
                velocityY = speed * (float) (dy / distance);
            } else {
                // If there are no more points in the path
                isAtPathEnd = true;
            }
        }

        //increment location based on velocity values
        location.x += velocityX * delta;
        location.y += velocityY * delta;
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
    protected void render(double lerp, Canvas canvas, Paint paint) {
        if (hp > 0) {
            float x = (float) Math.round(location.x + velocityX * lerp);
            float y = (float) Math.round(location.y + velocityY * lerp);

            // Draw the Enemy bitmap image
            canvas.drawBitmap(bitmap, x - bitmap.getWidth() / 2f,
                y - bitmap.getHeight() / 2f, null);

            // Draw the Enemy hp above the bitmap
            int offset = 10;

            paint.reset();
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(50);
            canvas.drawText("HP: " + hp, x, y - offset - bitmap.getHeight() / 2f, paint);
        }
    }

    /**
     * A method to remove hp from the Enemy
     *
     * @param damage The amount of hp to remove
     */
    public void takeDamage(int damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            this.isAlive = false;
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

    public boolean isAlive() {
        return this.isAlive;
    }

    public boolean isAtPathEnd() {
        return this.isAtPathEnd;
    }
}