package com.wsu.towerdefense;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

/**
 * Class that controls the Tower objects on the Map
 */
public class Tower extends AbstractMapObject {

    public Tower(PointF location, Bitmap bitmap) {
        super(location, bitmap);
    }

    /**
     * Updates the Tower based on the the change in time
     * since the Tower was last updated.
     *
     * @param game  the Game object this Tower belongs to
     * @param delta amount of time that has passed between updates
     */
    @Override
    protected void update(Game game, double delta) {
    }

    /**
     * Draws this Tower's Bitmap image to the provided Canvas.
     *
     * @param lerp   interpolation factor
     * @param canvas the canvas this Tower will be drawn on
     * @param paint  the paint object used to paint onto the canvas
     */
    @Override
    protected void render(double lerp, Canvas canvas, Paint paint) {
        canvas.drawBitmap(bitmap, location.x, location.y, null);
    }
}
