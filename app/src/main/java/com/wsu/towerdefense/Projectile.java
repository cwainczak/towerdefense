package com.wsu.towerdefense;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import static com.google.android.material.math.MathUtils.lerp;



public class Projectile extends AbstractMapObject {

    // What percent of the bitmap height will be used for the hitbox
    private static final float hitboxScaleY = 0.8f;

    // What percent of the bitmap width will be used for the hitbox
    private static final float hitboxScaleX = 0.8f;

    enum Type {

        HOMING(750f, R.mipmap.projectile),
        LINEAR(1000f, R.mipmap.projectile);

        final float speed;
        final int resourceID;

        /**
         * @param someSpeed The speed of the projectile
         * @param someResourceID    The Resource ID of the image of the projectile
         */
        Type(float someSpeed, int someResourceID){
            this.speed = someSpeed;
            this.resourceID = someResourceID;
        }
    }

    private final Type type;
    private Enemy target;   // Each projectile will have a target at some point, regardless of type
    private float velX;     // Velocity X of the LINEAR type projectiles, based on the target's initial position
    private float velY;     // Velocity Y of the LINEAR type projectiles, based on the target's initial position

    /**
     * A projectile shot by Towers at Enemies
     *
     * @param location   A PointF representing the location of the bitmap's center
     * @param target     The Enemy this projectile is targeting (if none, value is null)
     */
    public Projectile(PointF location, Type pt, Enemy target) {
        super(location, pt.resourceID);
        this.type = pt;
        this.target = target;
        if (this.type == Type.LINEAR){
            this.setVelocity();
        }
    }

    public void update(Game game, double delta) {
        if (this.type == Type.HOMING){
            double distanceToTarget = Math.hypot(Math.abs(location.x - target.location.x), Math.abs(location.y - target.location.y));
            double distanceMoved = this.type.speed * delta;

            // If the projectile moved far enough to reach the target set it at the target location
            if (distanceMoved >= distanceToTarget) {
                location.set(target.location);
            } else {
                // Otherwise move the projectile towards the target
                location.set(calculateNewLocation(delta));
            }
        }
        else if (this.type == Type.LINEAR){
            boolean hasHit = false;
            for (Enemy e : game.getEnemies()){
                this.target = e;
                if (this.hitTarget()){
                    location.set(target.location);
                    hasHit = true;
                }
            }
            if (!hasHit){
                this.location.set(this.calculateNewLocation(delta));
            }
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
        if (this.type == Type.HOMING) {
            double distanceToTarget = Math.hypot(
                    Math.abs(location.x - target.location.x),
                    Math.abs(location.y - target.location.y)
            );
            double distanceMoved = this.type.speed * delta;

            float amount = (float) (distanceMoved / distanceToTarget);

            return new PointF(
                    lerp(location.x, target.location.x, amount),
                    lerp(location.y, target.location.y, amount)
            );
        }
        else if (this.type == Type.LINEAR){
            double xDistanceMoved = this.velX * delta;
            double yDistanceMoved = this.velY * delta;
            return new PointF((float) (this.location.x + xDistanceMoved), (float) (this.location.y + yDistanceMoved));
        }
        return null;
    }

    public Enemy getTarget() {
        return target;
    }

    public boolean isOffScreen(int screenWidth, int screenHeight){
        return (this.location.x < 0 || this.location.x > screenWidth ||
                this.location.y < 0 || this.location.y > screenHeight);
    }

    private void setVelocity(){
        float dx = this.target.location.x - this.location.x;
        float dy = this.target.location.y - this.location.y;
        double distance = Math.hypot(dx, dy);
        this.velX = type.speed * (float) (dx / distance);
        this.velY = type.speed * (float) (dy / distance);
    }

}
