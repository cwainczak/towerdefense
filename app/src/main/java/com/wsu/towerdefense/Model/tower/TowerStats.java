package com.wsu.towerdefense.Model.tower;

import android.content.Context;
import android.graphics.Bitmap;
import com.wsu.towerdefense.Application;
import com.wsu.towerdefense.Model.Projectile;
import com.wsu.towerdefense.Model.tower.Upgrade.Effect;
import com.wsu.towerdefense.Model.tower.Upgrade.StatType;
import com.wsu.towerdefense.Util;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the stats and upgrades of a particular {@link Tower Tower} instance
 * <p>
 * Numeric modifiers are multiplied by the base stat value from {@link Tower.Type} to obtain the
 * effective (final) stat value. {@link #projectileType} overrides the {@link Projectile.Type} in
 * the tower's base stats.
 * <p>
 * To prevent excessive recalculations, numeric modifiers are cached in {@link #range}, {@link
 * #fireRate}, {@link #projectileSpeed}, and {@link #projectileDamage}, and are only recalculated
 * when a new upgrade with the same stat type is applied (see {@link #recache(Upgrade)}.
 */
public class TowerStats implements Serializable {

    private static final float REFUND_PERCENT = 0.5f;

    private final Tower.Type type;

    private transient TowerUpgradeData upgradeData;
    /**
     * Number of upgrades in each path. <code>0</code> represents no upgrades
     */
    private final int[] upgradeProgress;

    private float range;
    private float fireRate;
    private float projectileSpeed;
    private float projectileDamage;
    private float projectileRange;
    private double projectileSlowTime;
    private double projectileSlowRate;
    private boolean canSeeInvisible;
    private int projectilePierce;
    private int sellPrice;
    private Projectile.Type projectileType;
    private int turretImageID;
    private transient Bitmap turretImage;

    public TowerStats(Context context, Tower.Type type) {
        this.type = type;

        this.upgradeData = UpgradeReader.get(type);
        this.upgradeProgress = new int[this.upgradeData.paths.length];

        this.range = type.range;
        this.fireRate = type.fireRate;
        this.projectileSpeed = 1;
        this.projectileDamage = 1;
        this.projectileRange = 1;
        this.projectilePierce = 0;
        this.projectileSlowTime = type.projectileType.slowEnemyTime;
        this.projectileSlowRate = type.projectileType.slowRate;
        this.sellPrice = (int) (type.cost * REFUND_PERCENT);
        this.canSeeInvisible = type.canSeeInvisible;
        this.projectileType = type.projectileType;
        this.turretImageID = type.towerResID;
        this.turretImage = Util.getBitmapByID(context, type.towerResID);
    }

    /**
     * Determine whether an upgrade is possible in the given <code>pathNumber</code>, based on path
     * rules
     *
     * @param pathNumber Number of the path to upgrade in
     * @return Whether an upgrade is possible
     */
    public boolean canUpgrade(int pathNumber) {
        // TODO: limit upgrades based on MAX_PATHS and LOCK_THRESHOLD
        return true;
    }

    /**
     * If possible, apply an upgrade to the tower based on the given <code>pathNumber</code>
     *
     * @param pathNumber Number of the path to upgrade in
     * @return {@link Upgrade} that was applied, or <code>null</code> if the upgrade was not applied
     */
    public Upgrade upgrade(int pathNumber) {
        if (pathNumber < upgradeData.paths.length) {
            Upgrade[] path = upgradeData.paths[pathNumber];
            if (upgradeProgress[pathNumber] < path.length && canUpgrade(pathNumber)) {
                upgradeProgress[pathNumber]++;

                Upgrade upgrade = getUpgrade(pathNumber, false);
                recache(upgrade);

                if (upgrade.imageID > turretImageID){
                    turretImageID = upgrade.imageID;
                    turretImage = upgrade.image;
                }

                sellPrice += (int) (upgrade.cost * REFUND_PERCENT);

                return upgrade;
            }
        }
        return null;
    }

    /**
     * Recalculate only relevant modifiers based on a given {@link Upgrade}
     *
     * @param upgrade Upgrade whose effects' corresponding stat modifiers are recalculated
     */
    private void recache(Upgrade upgrade) {
        for (Effect<?> effect : upgrade.effects) {
            List<Effect<?>> activeEffects = getActiveEffects(effect.type);

            switch (effect.type) {
                case RANGE: {
                    this.range = type.range * getModifier(activeEffects);
                    break;
                }
                case FIRE_RATE: {
                    this.fireRate = type.fireRate * getModifier(activeEffects);
                    break;
                }
                case PROJECTILE_SPEED: {
                    this.projectileSpeed = getModifier(activeEffects);
                    break;
                }
                case PROJECTILE_DAMAGE: {
                    this.projectileDamage = getModifier(activeEffects);
                    break;
                }
                case PROJECTILE_RANGE: {
                    this.projectileRange = getModifier(activeEffects);
                    break;
                }
                case PROJECTILE_PIERCE: {
                    this.projectilePierce += getModifier(activeEffects);
                    break;
                }
                case PROJECTILE: {
                    this.projectileType = (Projectile.Type) effect.value;
                    this.projectileSlowRate = projectileType.slowRate;
                    this.projectileSlowTime = projectileType.slowEnemyTime;
                    break;
                }
                case SEE_INVISIBLE: {
                    this.canSeeInvisible = (Boolean) effect.value;
                    break;
                }
                case SLOW_TIME: {
                    this.projectileSlowTime = getModifier(activeEffects);
                    break;
                }
                case SLOW_RATE: {
                    this.projectileSlowRate += getModifier(activeEffects) - 1;
                    break;
                }
            }
        }
    }

    /**
     * Calculate the final stat modifier for a set of numeric <code>effects</code>
     * <p>
     * e.g.:
     * <pre>
     * {@code
     * effects = {0.2, 0.5, -0.1}
     * final_modifier = 1 + (+0.2) + (+0.5) + (-0.1)
     * = 1.6 // equivalent to 60% increase
     * }
     * </pre>
     *
     * @param effects List of effects to calculate final modifier from
     * @return Modifier by which base stat is multiplied
     */
    private float getModifier(List<Effect<?>> effects) {
        float mod = 1.0f;

        // TODO
        for (Effect effect : effects) {
            mod += (float) effect.value;
        }

        if (mod < 0) {
            mod = 0;
        }

        return mod;
    }

    /**
     * Get a list of active upgrades across all upgrade paths
     *
     * @return Active upgrades
     */
    private List<Upgrade> getActiveUpgrades() {
        List<Upgrade> upgrades = new ArrayList<>();

        for (int pathIndex = 0; pathIndex < upgradeData.paths.length; pathIndex++) {
            for (int i = 0; i < upgradeProgress[pathIndex]; i++) {
                Upgrade upgrade = upgradeData.paths[pathIndex][i];
                upgrades.add(upgrade);
            }
        }

        return upgrades;
    }

    /**
     * Get a list of active effects of a given stat <code>type</code> across all upgrade paths
     *
     * @param type Stat type to filter effects by
     * @return List of active effects
     */
    private List<Effect<?>> getActiveEffects(StatType type) {
        List<Effect<?>> effects = new ArrayList<>();

        List<Upgrade> activeUpgrades = getActiveUpgrades();
        for (Upgrade upgrade : activeUpgrades) {
            for (Effect<?> effect : upgrade.effects) {
                if (effect.type == type) {
                    effects.add(effect);
                }
            }
        }

        return effects;
    }

    public float getRange() {
        return range;
    }

    public float getFireRate() {
        return fireRate;
    }

    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    public float getProjectileDamage() {
        return projectileDamage;
    }

    public float getProjectileRange() {
        return projectileRange;
    }

    public double getProjectileSlowTime() {
        return projectileSlowTime;
    }

    public double getProjectileSlowRate() { return projectileSlowRate; }

    public int getProjectilePierce(){
        return projectilePierce;
    }

    public boolean canSeeInvisible() {
        return canSeeInvisible;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public Projectile.Type getProjectileType() {
        return projectileType;
    }

    public Bitmap getTurretImage() {
        return turretImage;
    }

    public int getUpgradeProgress(int pathNumber) {
        return upgradeProgress[pathNumber];
    }

    public Upgrade getUpgrade(int pathNumber, boolean next) {
        Upgrade[] path = upgradeData.paths[pathNumber];
        int upgradeIndex = upgradeProgress[pathNumber] - 1 + (next ? 1 : 0);
        return path[upgradeIndex];
    }

    public boolean isMaxUpgraded(int pathNumber) {
        return upgradeProgress[pathNumber] == upgradeData.paths[pathNumber].length;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        upgradeData = UpgradeReader.get(type);
        turretImage = Util.getBitmapByID(Application.context, turretImageID);
    }
}
