package com.wsu.towerdefense;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Util {

    /**
     * Hides the systemUi navigator bar
     *
     * @param window
     */
    public static void hideNavigator(Window window) {
        window.getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public static float adjustVolume(float volume) {
        final float MAX_VOLUME = 100.0f;
        float adj = (float) (1 - (Math.log(MAX_VOLUME - volume) / Math.log(MAX_VOLUME)));
        if (adj > 2) {
            adj = 2;
        }
        return adj;
    }

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
    public static PointF velocityTowardsPoint(PointF curLoc, PointF targetLoc, float speed) {
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

    /**
     * Calculates the angle between the line drawn between two points and the horizontal axis.
     *
     * @param start The point that starts the line
     * @param end   The point that ends the line
     * @return The angle between the line and the horizontal axis
     */
    public static double getAngleBetweenPoints(PointF start, PointF end) {
        // TODO: keep in radians?
        double deltaY = (end.y - start.y);
        double deltaX = (end.x - start.x);
        double result = Math.toDegrees(Math.atan2(deltaY, deltaX));
        return (result < 0) ? (360d + result) : result;
    }

    public static int getResourceByName(Context context, String type, String name) {
        return context.getResources().getIdentifier(
            name,
            type,
            context.getPackageName()
        );
    }

    public static Bitmap getBitmapByID(Context context, int resourceID) {
        return BitmapFactory.decodeResource(
            context.getResources(),
            resourceID
        );
    }

    public static int dpToPixels(Resources resources, int dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.getDisplayMetrics()
        );
    }

    public static PointF rotatePoint(PointF point, double angle) {
        return rotatePoint(point, new PointF(0, 0), angle);
    }

    public static PointF rotatePoint(PointF point, PointF origin, double angle) {
        double a = Math.toRadians(angle);

        float dx = point.x - origin.x;
        float dy = point.y - origin.y;

        return new PointF(
            (float) (Math.cos(a) * dx - Math.sin(a) * dy + origin.x),
            (float) (Math.sin(a) * dx + Math.cos(a) * dy + origin.y)
        );
    }
}
