package com.wsu.towerdefense;

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


}
