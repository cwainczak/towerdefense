package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import com.wsu.towerdefense.Model.Game;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Base class for MapObjects, such as Tower and Enemy Objects
 */
public abstract class AbstractMapObject implements Serializable {

    /**
     * represents the location of the object on the screen
     */
    protected transient PointF location;
    /**
     * represents the image/shape of the object
     */
    protected transient Bitmap bitmap;

    private final int resourceID;

    public AbstractMapObject(Context context, PointF location, int resourceID) {
        this.location = location;
        this.resourceID = resourceID;
        this.bitmap = Util.getBitmapByID(context, resourceID);
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

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeFloat(this.location.x);
        out.writeFloat(this.location.y);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        float x = in.readFloat();
        float y = in.readFloat();

        this.bitmap = BitmapFactory.decodeResource(
            Application.context.getResources(),
            this.resourceID
        );
        this.location = new PointF(x, y);
    }
}
