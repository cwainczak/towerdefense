package com.wsu.towerdefense;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

/**
 * Base class for MapObjects, such as Tower and Enemy Objects
 */
public abstract class AbstractMapObject {

    /**
     * represents the location of the object on the screen
     */
    protected PointF location;
    /**
     * represents the image/shape of the object
     */
    protected Bitmap bitmap;

    public AbstractMapObject(PointF someLocation, Bitmap someBitmap) {
        this.setLocation(someLocation);
        this.setBitmap(someBitmap);
    }

    /**
     * @param delta amount of time that has passed between updates
     */
    protected abstract void update(Game game, double delta);

    /**
     * @param lerp interpolation factor
     */
    protected abstract void render(double lerp, Canvas canvas, Paint paint);

    public PointF getLocation() {
        return this.location;
    }

    public void setLocation(PointF location) {
        this.location = location;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
