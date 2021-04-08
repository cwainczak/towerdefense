package com.wsu.towerdefense.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import com.wsu.towerdefense.Util;
import java.util.Collections;
import java.util.List;

/**
 * A map that has not been adjusted for screen size
 */
public class AbstractMap {

    protected final String name;
    protected final String displayName;
    protected final int imageID;
    protected final Bitmap image;
    protected final List<PointF> path;
    protected final float pathRadius;

    public AbstractMap(Context context, String name, String displayName, int imageID,
        List<PointF> path, float pathRadius) {
        this(name, displayName, imageID, Util.getBitmapByID(context, imageID), path, pathRadius);
    }

    public AbstractMap(String name, String displayName, int imageID, Bitmap image,
        List<PointF> path, float pathRadius) {
        this.name = name;
        this.displayName = displayName;
        this.imageID = imageID;
        this.image = image;
        this.path = path;
        this.pathRadius = pathRadius;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getImageID() {
        return imageID;
    }

    public List<PointF> getPath() {
        return Collections.unmodifiableList(path);
    }
}
