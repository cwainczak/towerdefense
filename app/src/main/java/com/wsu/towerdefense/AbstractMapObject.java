package com.wsu.towerdefense;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.Image;

/**
 * Base class for MapObjects, such as Tower and Enemy Objects
 */
public abstract class AbstractMapObject {

    /**
     * represents the location of the object on the screen
     */
    private Point location;
    /**
     * represents the image/shape of the object
     */
    private Image image;

    public AbstractMapObject(Point someLocation, Image someImage) {
        this.setLocation(someLocation);
        this.setImage(someImage);
    }

    /**
     * @param delta amount of time that has passed between updates
     */
    protected abstract void update(Game game, double delta);

    /**
     * @param lerp interpolation factor
     */
    protected abstract void render(double lerp, Canvas canvas, Paint paint);

    public Point getLocation() {
        return this.location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public Image getImage() {
        return this.image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
