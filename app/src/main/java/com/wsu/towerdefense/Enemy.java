package com.wsu.towerdefense;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

/**
 * Class that controls the Enemy objects on the Map
 */
public class Enemy extends AbstractMapObject {
    private float velocityX;    // Enemy's velocity in the x direction
    private float velocityY;    // Enemy's velocity in the y direction

    /**
     * @param location  A PointF representing the location of the Enemy bitmap's top left corner
     * @param bitmap    A bitmap image of the Enemy object
     * @param velocityX The Enemy's velocity in the x direction
     * @param velocityY The Enemy's velocity in the y direction
     */
    public Enemy(PointF location, Bitmap bitmap, float velocityX, float velocityY) {
        super(location, bitmap);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
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
        if (location.x >= game.getDisplayWidth() - bitmap.getWidth() || location.x < 0) {
            velocityX *= -1;
        }
        location.x += velocityX * delta;
        location.y += velocityY * delta;
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
}
