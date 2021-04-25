package com.wsu.towerdefense.Model.tower;

import android.content.Context;
import android.graphics.Bitmap;
import com.wsu.towerdefense.Util;

/**
 * A static upgrade that can be applied to a tower
 * <p>
 * See documentation for upgrade format in {@link UpgradeReader}
 */
public class Upgrade {

    public enum StatType {
        RANGE,
        FIRE_RATE,
        PROJECTILE_SPEED,
        PROJECTILE_DAMAGE,
        PROJECTILE_RANGE,
        PROJECTILE_PIERCE,
        PROJECTILE,
        SEE_INVISIBLE,
        SLOW_TIME,
        SLOW_RATE,
    }

    public static class Effect<T> {

        public final StatType type;
        public final T value;

        public Effect(StatType type, T value) {
            this.type = type;
            this.value = value;
        }
    }

    public final String displayName;
    public final String description;
    public final int cost;
    public final int imageID;
    public final Bitmap image;
    public final Effect<?>[] effects;

    public Upgrade(Context context, String displayName, String description, int cost, int imageID,
        Effect<?>[] effects) {
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
        this.imageID = imageID;
        this.image = Util.getBitmapByID(context, imageID);
        this.effects = effects;
    }
}
