package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import java.util.List;
import java.util.ListIterator;

public class Enemy extends AbstractMapObject {

    protected enum Type {
        //Standard enemy types
        S1(200, 10, 25, false, R.mipmap.red_slime),
        S2(250, 15, 35, true, R.mipmap.translucent_slime),
        S3(400, 30 , 45, false, R.mipmap.green_slime);

        final float speed;
        final int hp;
        final int price;
        final boolean invisible;
        final int resource;

        Type(float speed, int hp, int price, boolean invisible, int resource) {
            this.speed = speed;
            this.hp = hp;
            this.price = price;
            this.invisible = invisible;
            this.resource = resource;
        }
    }

    private final Type type;

    private int hp;
    private boolean isAlive;
    private boolean isInvisible;

    private float velX;
    private float velY;

    private final ListIterator<PointF> path;
    private boolean isAtPathEnd;
    private PointF target;

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

        if (distance <= Math.abs(type.speed * delta)) {
            if (path.hasNext()) {
                //set location to target, and update target
                location = new PointF(target.x, target.y);
                target = path.next();

                PointF newVel = Util.getNewVelocity(this.location, this.target, this.type.speed);
                this.velX = newVel.x;
                this.velY = newVel.y;
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
    protected void render(double lerp, Canvas canvas, Paint paint) {
        if (hp > 0) {
            float x = (float) Math.round(location.x + velX * lerp);
            float y = (float) Math.round(location.y + velY * lerp);

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

    public boolean isInvisible() { return isInvisible; }

}