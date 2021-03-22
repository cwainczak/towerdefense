package com.wsu.towerdefense;

import static com.google.android.material.math.MathUtils.lerp;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PointF;
import java.util.List;

public class Projectile extends AbstractMapObject {

    // What percent of the bitmap height will be used for the hitbox
    private static final float hitboxScaleY = 0.8f;

    // What percent of the bitmap width will be used for the hitbox
    private static final float hitboxScaleX = 0.8f;

    enum Type {

        LINEAR(1000f, 10, R.mipmap.projectile_1),
        HOMING(750f, 15, R.mipmap.projectile_2);

        final float speed;
        final int damage;
        final int resourceID;

        /**
         * @param someSpeed The speed of the projectile_1
         * @param damage    Damage done to an Enemy hit by this projectile_1
         * @param someResourceID    The Resource ID of the image of the projectile_1
         */
        Type(float someSpeed, int damage, int someResourceID){
            this.speed = someSpeed;
            this.damage = damage;
            this.resourceID = someResourceID;
        }
    }

    private final Type type;
    private final Enemy target;
    private float velX;     // Velocity X of the LINEAR type projectiles, based on the target's initial position
    private float velY;     // Velocity Y of the LINEAR type projectiles, based on the target's initial position
    public boolean remove;
    private PointF front;


    /**
     * A projectile_1 shot by Towers at Enemies
     *
     * @param location   A PointF representing the location of the bitmap's center
     * @param target     The Enemy this projectile_1 is targeting (if none, value is null)
     */
    public Projectile(PointF location, Type pt, Enemy target) {
        super(location, pt.resourceID);
        this.type = pt;
        this.target = target;
        if (this.type == Type.LINEAR){
            PointF newVel = Util.getNewVelocity(this.location, this.target.location, this.type.speed);
            this.velX = newVel.x;
            this.velY = newVel.y;
        }

        front = new PointF(location.x - bitmap.getWidth()/2f, location.y - bitmap.getHeight()/2f);
    }

    public void update(Game game, double delta) {
        if (this.type == Type.HOMING){
            double distanceToTarget = Math.hypot(Math.abs(location.x - target.location.x), Math.abs(location.y - target.location.y));
            double distanceMoved = this.type.speed * delta;

            // If the projectile moved far enough to reach the target set it at the target location
            if (distanceMoved >= distanceToTarget) {
                location.set(target.location);
            } else {
                PointF newLoc = calculateNewLocation(delta);



                location.set(calculateNewLocation(delta));
            }
        }
        else if (this.type == Type.LINEAR){
            location.set(calculateNewLocation(delta));
        }

        checkCollision(game.getEnemies());

        if (isOffScreen(game.getGameWidth(), game.getGameHeight())) {
            remove = true;
        }
    }

    @Override
    protected void render(double lerp, Canvas canvas, Paint paint) {
        if (!remove) {
            PointF newLoc = calculateNewLocation(lerp);

            Matrix matrix = new Matrix();
            matrix.postRotate((float) Util.getAngleBetweenPoints(newLoc, target.location) + 90,
                    bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);

            matrix.postTranslate(newLoc.x - bitmap.getWidth() / 2f, newLoc.y - bitmap.getHeight() / 2f);

            canvas.drawBitmap(bitmap, matrix, null);
        }
    }

    private void checkCollision(List<Enemy> enemies) {
        for (Enemy e : enemies) {
            if (e.collides(location.x, location.y,
                    bitmap.getWidth() * hitboxScaleX,
                    bitmap.getHeight() * hitboxScaleY)) {

                e.takeDamage(type.damage);
                remove = true;
                break;
            }
        }
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
                return Util.getNewLoc(this.location, this.velX, this.velY, delta);
        }
        return null;
    }

    private boolean isOffScreen(int screenWidth, int screenHeight){
        return  this.location.x < 0 || this.location.x > screenWidth ||
                this.location.y < 0 || this.location.y > screenHeight;
    }
}
