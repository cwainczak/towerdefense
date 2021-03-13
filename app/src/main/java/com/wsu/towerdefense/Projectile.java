package com.wsu.towerdefense;

import static com.google.android.material.math.MathUtils.lerp;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class Projectile extends AbstractMapObject {

    // What percent of the bitmap height will be used for the hitbox
    private static final float hitboxScaleY = 0.8f;

    // What percent of the bitmap width will be used for the hitbox
    private static final float hitboxScaleX = 0.8f;

    private final float speed;
    private final Enemy target;

    /**
     * A projectile shot by Towers at Enemies
     *
     * @param location   A PointF representing the location of the bitmap's center
     * @param speed      The Projectile's velocity
     * @param resourceID The resource ID of the image of this Projectile object
     * @param target     The Enemy this projectile is targeting
     */
    public Projectile(PointF location, int resourceID, float speed, Enemy target) {
        super(location, resourceID);
        this.speed = speed;
        this.target = target;
    }

    public void update(Game game, double delta) {
        double distanceToTarget = Math.hypot(Math.abs(location.x - target.location.x),
            Math.abs(location.y - target.location.y));
        double distanceMoved = speed * delta;

        // If the projectile moved far enough to reach the target set it at the target location
        if (distanceMoved >= distanceToTarget) {
            location.set(target.location);
        } else {
            // Otherwise move the projectile towards the target
            location.set(calculateNewLocation(delta));
        }
    }

    @Override
    protected void render(double lerp, Canvas canvas, Paint paint) {
        PointF newLoc = calculateNewLocation(lerp);
        if (!hitTarget(newLoc.x, newLoc.y)) {
            canvas.drawBitmap(bitmap,
                newLoc.x - bitmap.getWidth() / 2f,
                newLoc.y - bitmap.getHeight() / 2f,
                null);
        }
    }

    /**
     * Deals damage to this Projectile's target.
     *
     * @param damage The amount of damage to deal.
     */
    public void damageTarget(int damage) {
        target.takeDamage(damage);
    }

    /**
     * A method that checks whether the hitbox of this Projectile overlaps with the hitbox of the
     * Enemy this Projectile is targeting.
     *
     * @return true if this Projectile hit its target, otherwise false
     */
    public boolean hitTarget() {
        return hitTarget(location.x, location.y);
    }

    private boolean hitTarget(float x, float y) {
        return target.collides(x, y,
            bitmap.getWidth() * hitboxScaleX,
            bitmap.getHeight() * hitboxScaleY
        );
    }

    /**
     * Calculates the Projectile's new position given a change in time
     *
     * @param delta The change in time since this Projectile's location has been updated
     * @return The new location this Projectile should move to
     */
    private PointF calculateNewLocation(double delta) {
        double distanceToTarget = Math.hypot(
            Math.abs(location.x - target.location.x),
            Math.abs(location.y - target.location.y)
        );
        double distanceMoved = speed * delta;

        float amount = (float) (distanceMoved / distanceToTarget);

        return new PointF(
            lerp(location.x, target.location.x, amount),
            lerp(location.y, target.location.y, amount)
        );
    }

    public Enemy getTarget() {
        return target;
    }
}
