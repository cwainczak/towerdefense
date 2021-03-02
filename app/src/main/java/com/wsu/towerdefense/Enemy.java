package com.wsu.towerdefense;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import java.util.List;
import java.util.ListIterator;

/**
 * Class that controls the Enemy objects on the Map
 */
public class Enemy extends AbstractMapObject {
    private float velocityX;    // Enemy's velocity in the x direction
    private float velocityY;    // Enemy's velocity in the y direction
    private float velocity;     // pixels per second

    private ListIterator<Point> path;
    private Point target;
    private double angle;

    private PointF cellSize;


    /**
     * @param path     A List of Points for the current location to move towards
     * @param cellSize Dimensions of each cell in the grid making up the map area
     * @param bitmap   A bitmap image of the Enemy object
     * @param velocity The Enemy's velocity
     */
    public Enemy(List<Point> path, PointF cellSize, Bitmap bitmap,
                 float velocity) {
        super(new PointF(path.get(0).x * cellSize.x , path.get(0).y * cellSize.y), bitmap);
        this.velocity = velocity;
        this.cellSize = cellSize;

        List<Point> tempPath = path;
        for(Point p : tempPath) {
            p.x *= cellSize.x;
            p.y *= cellSize.y;
        }
        this.path = tempPath.listIterator();
        this.target = this.path.next();

        this.angle = Math.atan2(target.y - location.y, target.x - location.x);

    }

    /**
     * Updates the Enemy's location based on the Enemy's velocity and the change in time
     * since the location was last updated.
     *
     * @param game  the Game object this Enemy belongs to
     * @param delta amount of time that has passed between updates
     */
    @Override
    protected void update(Game game, double delta) {
        moveEnemy(delta);
    }

    /**
     * Draws this Enemy's Bitmap image to the provided Canvas, interpolating changes in position
     * to maintain smooth movement regardless of updates since last drawn.
     *
     * @param lerp   interpolation factor
     * @param canvas the canvas this Enemy will be drawn on
     * @param paint  the paint object used to paint onto the canvas
     */
    @Override
    protected void render(double lerp, Canvas canvas, Paint paint) {
        float x = (float) Math.round(location.x + velocityX * lerp);
        float y = (float) Math.round(location.y + velocityY * lerp);

        canvas.drawBitmap(bitmap, x, y, null);
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
