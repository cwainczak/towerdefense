package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Enemy extends AbstractMapObject {
    private float velocityX;    // Enemy's velocity in the x direction
    private float velocityY;    // Enemy's velocity in the y direction
    private float speed;     // pixels per second

    private ListIterator<Point> path;
    private Point target;
    private PointF cellSize;

    private PointF offset;


    /**
     * Is the enemy alive
     */
    boolean isAlive;

    /**
     * Did the enemy reach the last point in its path
     */
    boolean isAtPathEnd;

    /**
     * Enemy's hit points
     */
    private int hp;

    /**
     * An Enemy is a movable Map object. Enemies will move along a predetermined path defined by the
     * Map they are placed on. They will continue moving along the path until they reach the end or
     * are killed by a Projectile.
     *
     * @param path      A List of Points for the current location to move towards
     * @param cellSize  Dimensions of each cell in the grid making up the map area
     * @param hp        The amount of hit points this Enemy has
     * @param speed     distance enemy moves, pixels per second
     */
    public Enemy(List<Point> path, PointF cellSize, int hp, float speed) {
        super(new PointF(path.get(0).x * cellSize.x + (cellSize.x/2),
                path.get(0).y * cellSize.y + (cellSize.y/2)), R.mipmap.enemy);

        this.speed = speed;
        this.cellSize = cellSize;
        this.offset = new PointF(cellSize.x / 2, cellSize.y / 2);

        this.path = path.listIterator();
        this.target = this.path.next();

        this.hp = hp;
        this.isAlive = true;
        this.isAtPathEnd = false;
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
            int offset = 10;

            paint.setColor(Color.RED);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(40);

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

    /**
     * Moves this Enemy to its next location using velocity and delta. If enemy will be at or past
     * target, and there are more Points in path, set location to target instead and set target to
     * next Point in path.
     *
     * @param delta Time since last update
     */
    private void moveEnemy(double delta) {
        //check if distance between location and target is less than or equal to distance between
        //location and next location, and there are more Points int path
        PointF pixTarget = new PointF(
                target.x * cellSize.x + offset.x,
                target.y * cellSize.y + offset.y
        );
        double distance = Math.hypot(location.x - pixTarget.x, location.y - pixTarget.y);

        if (distance <= Math.abs(speed * delta)) {
            if (path.hasNext()) {
            //set location to target, and update target
                location = pixTarget;
                target = path.next();
                pixTarget = new PointF(
                        target.x * cellSize.x + offset.x,
                        target.y * cellSize.y + offset.y
                );

                float dx = pixTarget.x - location.x;
                float dy = pixTarget.y - location.y;
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
}