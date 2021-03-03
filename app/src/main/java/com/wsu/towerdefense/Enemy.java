package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import java.util.List;
import java.util.ListIterator;

public class Enemy extends AbstractMapObject {
    private float velocityX;    // Enemy's velocity in the x direction
    private float velocityY;    // Enemy's velocity in the y direction
    private float velocity;     // pixels per second

    private ListIterator<Point> path;
    private Point target;
    private double angle;
    private PointF cellSize;


    /**
     * Is the enemy alive
     */
    boolean isAlive;
    /**
     * Enemy's hit points
     */
    private int hp;

    /**
     * An Enemy is a movable Map object. Enemies will move along a predetermined path defined by the
     * Map they are placed on. They will continue moving along the path until they reach the end or
     * are killed by a Projectile.
     *
     * @param hp        The amount of hit points this Enemy has
     * @param path     A List of Points for the current location to move towards
     * @param cellSize Dimensions of each cell in the grid making up the map area
     * @param velocity The Enemy's velocity
     */
    public Enemy(List<Point> path, PointF cellSize,
                 int hp, float velocity) {
        super(new PointF(path.get(0).x * cellSize.x, path.get(0).y * cellSize.y), R.mipmap.enemy);
        Log.i("--contrtuct--", "constructor called");
        this.velocity = velocity;
        this.cellSize = cellSize;

        // generate path
        List<Point> tempPath = path;
        for (Point p : tempPath) {
            Log.i("--size     --", path.size() + "");
            Log.i("--regpath  --", p.x + " " + p.y);
            p.x *= cellSize.x;
            p.y *= cellSize.y;
            Log.i("--pixpath  --", p.x + " " + p.y);
        }
        this.path = tempPath.listIterator();
        this.target = this.path.next();

        this.angle = Math.atan2(target.y - location.y, target.x - location.x);

        this.hp = hp;
        this.isAlive = true;
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
        Log.i("--update   --", "updating enemy");
        moveEnemy(delta);
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
            float textSize = paint.getTextSize();
            int textScale = 4;
            int offset = 10;

            paint.setColor(Color.RED);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(textSize * textScale);

            canvas.drawText("HP: " + hp, x, y - offset - bitmap.getHeight() / 2f, paint);
            paint.setTextSize(textSize);
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

    /**
     * Moves this Enemy to its next location using velocity and delta. If enemy will be at or past
     * target, and there are more Points in path, set location to target instead and set target to
     * next Point in path.
     *
     * @param delta Time since last update
     */
    private void moveEnemy(double delta) {
        Log.i("--location --", location.toString());
        Log.i("--target   --", target.toString());
        Log.i("--angle    --", angle + "");

        //check if distance between location and target is less than or equal to distance between
        //location and next location, and there are more Points int path
        if (Math.hypot(location.x - target.x, location.y - target.y) <= Math.abs(velocity * delta)
                && path.hasNext()) {

            //set location to target, and update target
            location = new PointF(target);
            target = path.next();
            angle = Math.atan2(target.y - location.y, target.x - location.x);
        } else {

            //get x and y velocities
            angle = Math.atan2(target.y - location.y, target.x - location.x);
            velocityX = (float) (Math.round(velocity * Math.cos(angle)));
            velocityY = (float) (Math.round(velocity * Math.sin(angle) ));

            //increment location based on velocity values
            location.x += velocityX * delta;
            location.y += velocityY * delta;
        }
    }
}
