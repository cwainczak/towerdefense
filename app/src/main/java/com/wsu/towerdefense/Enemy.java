package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

public class Enemy extends AbstractMapObject {

    /**
     * Enemy's velocity in the x direction
     */
    private float velocityX;
    /**
     * Enemy's velocity in the y direction
     */
    private float velocityY;

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
     * @param location  A PointF representing the location of the Enemy bitmap's center
     * @param velocityX The Enemy's velocity in the x direction
     * @param velocityY The Enemy's velocity in the y direction
     * @param hp        The amount of hit points this Enemy has
     */
    public Enemy(PointF location, float velocityX, float velocityY, int hp) {
        super(location, R.mipmap.enemy);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
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
        if (location.x >= game.getDisplayWidth() - bitmap.getWidth() / 2f
            || location.x < bitmap.getWidth() / 2f) {
            velocityX *= -1;
        }
        if (location.y >= game.getDisplayHeight() - bitmap.getHeight() / 2f
            || location.y < bitmap.getHeight() / 2f) {
            velocityY *= -1;
        }
        location.x += velocityX * delta;
        location.y += velocityY * delta;
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
            canvas
                .drawBitmap(bitmap, x - bitmap.getWidth() / 2f, y - bitmap.getHeight() / 2f, null);

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
}
