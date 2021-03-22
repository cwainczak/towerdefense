package com.wsu.towerdefense;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;

/**
 * Util class for calculations
 */
public class Util {

    /**
     * Gets new location based on the velocity and delta (time passed since last update)
     * @param curLoc    Current location of the object
     * @param velX      Velocity of the Object in the X direction
     * @param velY      Velocity of the Object in the Y direction
     * @param delta     Time passed since last update in seconds
     * @return
     */
    public static PointF getNewLoc(PointF curLoc, float velX, float velY, double delta){
        double xDistanceMoved = velX * delta;
        double yDistanceMoved = velY * delta;
        return new PointF((float) (curLoc.x + xDistanceMoved), (float) (curLoc.y + yDistanceMoved));
    }

    /**
     * Gets velocity based on distance to target.
     * @param curLoc       Current Location of the object
     * @param targetLoc    Current location of the target (next coordinate on path)
     * @param speed        Speed of the object
     */
    public static PointF getNewVelocity(PointF curLoc, PointF targetLoc, float speed){
        float dx = targetLoc.x - curLoc.x;
        float dy = targetLoc.y - curLoc.y;
        double distance = Math.hypot(dx, dy);
        float velX = speed * (float) (dx / distance);
        float velY = speed * (float) (dy / distance);
        return new PointF(velX, velY);
    }

    /**
     * Calculates the angle between the line drawn between two points and the horizontal axis.
     * @param start The point that starts the line
     * @param end   The point that ends the line
     * @return      The angle between the line and the horizontal axis
     */
    public static double getAngleBetweenPoints(PointF start, PointF end) {
        double deltaY = (end.y - start.y);
        double deltaX = (end.x - start.x);
        double result = Math.toDegrees(Math.atan2(deltaY, deltaX));
        return (result < 0) ? (360d + result) : result;
    }
}
