package com.wsu.towerdefense.upgrade;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.wsu.towerdefense.Projectile;

/**
 * A static upgrade that can be applied to a tower
 * <p>
 * See documentation for upgrade format in {@link UpgradeReader}
 *
 * @param <T> the type of the value that is upgraded
 */
public class Upgrade<T> {

    public enum Effect {
        RANGE,
        FIRE_RATE,
        PROJECTILE_VELOCITY,
        PROJECTILE_DAMAGE,
        PROJECTILE
    }

    public final String displayName;
    public final String description;
    public final int cost;
    public final Effect effect;
    public final Bitmap image;
    public final T value;

    public Upgrade(Context context, String displayName, String description, int cost, int imageID,
        Effect effect,
        T value) {
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
        this.image = BitmapFactory.decodeResource(context.getResources(), imageID);
        this.effect = effect;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Upgrade{" +
            "displayName='" + displayName + '\'' +
            ", description='" + description + '\'' +
            ", cost=" + cost +
            ", effect=" + effect +
            ", image=" + image +
            ", value=" + value +
            '}';
    }
}
