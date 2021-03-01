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

    boolean isAlive;    // Is the enemy alive
    private int hp = 100; // Enemy's hit points

    /**
     * @param location  A PointF representing the location of the Enemy bitmap's center
     * @param bitmap    A bitmap image of the Enemy object
     * @param velocityX The Enemy's velocity in the x direction
     * @param velocityY The Enemy's velocity in the y direction
     */
    public Enemy(PointF location, Bitmap bitmap, float velocityX, float velocityY) {
        super(location, bitmap);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.isAlive = true;
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
        if (location.x >= game.getDisplayWidth() - bitmap.getWidth() / 2 || location.x < bitmap.getWidth() / 2) {
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

        canvas.drawBitmap(bitmap, x - bitmap.getWidth() / 2, y - bitmap.getHeight() / 2, null);
    }

    public void takeDamage(int damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            this.isAlive = false;
        }
    }

    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

}
