package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.PointF;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Util class for calculations
 */
public class Util {

    /**
     * Gets new location based on the velocity and delta (time passed since last update)
     *
     * @param curLoc Current location of the object
     * @param velX   Velocity of the Object in the X direction
     * @param velY   Velocity of the Object in the Y direction
     * @param delta  Time passed since last update in seconds
     * @return
     */
    public static PointF getNewLoc(PointF curLoc, float velX, float velY, double delta) {
        double xDistanceMoved = velX * delta;
        double yDistanceMoved = velY * delta;
        return new PointF((float) (curLoc.x + xDistanceMoved), (float) (curLoc.y + yDistanceMoved));
    }

    /**
     * Gets velocity based on distance to target.
     *
     * @param curLoc    Current Location of the object
     * @param targetLoc Current location of the target (next coordinate on path)
     * @param speed     Speed of the object
     */
    public static PointF getNewVelocity(PointF curLoc, PointF targetLoc, float speed) {
        float dx = targetLoc.x - curLoc.x;
        float dy = targetLoc.y - curLoc.y;
        double distance = Math.hypot(dx, dy);
        float velX = speed * (float) (dx / distance);
        float velY = speed * (float) (dy / distance);
        return new PointF(velX, velY);
    }

    /**
     * Read a file from the assets directory
     *
     * @param context  context
     * @param fileName file within 'assets/' to read
     * @return contents of the file
     * @throws IOException when file is invalid
     */
    public static String readFile(Context context, String fileName) throws IOException {
        InputStream stream = context.getAssets().open(fileName);

        return new BufferedReader(new InputStreamReader(stream)).lines()
            .collect(Collectors.joining("\n"));
    }

    public static int getResourceByName(Context context, String name) {
        return context.getResources().getIdentifier(
            name,
            "mipmap",
            context.getPackageName()
        );
    }
}
