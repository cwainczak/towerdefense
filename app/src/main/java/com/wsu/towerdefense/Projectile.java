package com.wsu.towerdefense;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import static com.google.android.material.math.MathUtils.lerp;

public class Projectile extends AbstractMapObject {

    float velocity;
    Enemy target;

    /**
     * A projectile shot by Towers at Enemies
     *
     * @param location A PointF representing the location of the bitmap's center
     * @param velocity The Projectile's velocity
     * @param bitmap   A bitmap image of this Projectile object
     * @param target   The Enemy this projectile is targeting
     */
    public Projectile(PointF location, float velocity, Bitmap bitmap, Enemy target) {
        super(location, bitmap);
        this.velocity = velocity;
        this.target = target;
    }

    public void update(Game game, double delta) {
        float a = Math.abs(location.x - target.location.x);
        float b = Math.abs(location.y - target.location.y);

        double distanceToTarget = Math.hypot(a, b);
        double distanceMoved = velocity * delta;

        // If the projectile moved far enough to reach the target set it at the target location
        if (distanceMoved >= distanceToTarget) {
            location.set(target.location);
        } else {
            // Otherwise move the projectile towards the target
            float amount = (float) (distanceMoved / distanceToTarget);

            float newX = lerp(location.x, target.location.x, amount);
            float newY = lerp(location.y, target.location.y, amount);

            location.set(newX, newY);
        }
    }

    public boolean hitTarget() {
        return location.x + bitmap.getWidth() / 3 >= target.location.x - target.bitmap.getWidth() / 2 &&
                location.x - bitmap.getWidth() / 3 <= target.location.x + target.bitmap.getWidth() / 2 &&
                location.y + bitmap.getHeight() / 3 <= target.location.y + target.bitmap.getHeight() / 2 &&
                location.y - bitmap.getHeight() / 3 >= target.location.y - target.bitmap.getHeight() / 2;
    }

    public void damageTarget(int damage) {
        target.takeDamage(damage);
    }

    @Override
    protected void render(double lerp, Canvas canvas, Paint paint) {
        float x = (float) Math.round(location.x);
        float y = (float) Math.round(location.y);

        canvas.drawBitmap(bitmap, x - bitmap.getWidth() / 2, y - bitmap.getHeight() / 2, null);
    }
}
