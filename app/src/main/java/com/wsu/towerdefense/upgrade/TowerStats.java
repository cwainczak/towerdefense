package com.wsu.towerdefense.upgrade;

import com.wsu.towerdefense.Projectile;
import com.wsu.towerdefense.Tower;
import com.wsu.towerdefense.upgrade.Upgrade.Effect;
import com.wsu.towerdefense.upgrade.Upgrade.StatType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the stats and upgrades of a particular {@link com.wsu.towerdefense.Tower Tower} instance
 * <p>
 * Numeric modifiers are multiplied by the base stat value from {@link Tower.Type} to obtain the
 * effective (final) stat value. {@link #projectileTypeModifier} overrides the {@link
 * Projectile.Type} in the tower's base stats.
 * <p>
 * To prevent excessive recalculations, numeric modifiers are cached in {@link #rangeModifier},
 * {@link #fireRateModifier}, {@link #projectileSpeedModifier}, and {@link
 * #projectileDamageModifier}, and are only recalculated when a new upgrade with the same stat type
 * is applied (see {@link #recache(Upgrade)}.
 */
public class TowerStats implements Serializable {

    private final Tower.Type type;

    private transient TowerUpgradeData upgradeData;
    /**
     * Number of upgrades in each path. <code>0</code> represents no upgrades
     */
    private final int[] upgradeProgress;

    private float rangeModifier;
    private float fireRateModifier;
    private float projectileSpeedModifier;
    private float projectileDamageModifier;
    private Projectile.Type projectileTypeModifier;

    public TowerStats(Tower.Type type) {
        this.type = type;

        this.upgradeData = UpgradeReader.get(type);
        this.upgradeProgress = new int[this.upgradeData.paths.length];

        this.rangeModifier = 1.0f;
        this.fireRateModifier = 1.0f;
        this.projectileSpeedModifier = 1.0f;
        this.projectileDamageModifier = 1.0f;
        this.projectileTypeModifier = null;
    }

    public int getUpgradeProgress(int pathNumber) {
        return upgradeProgress[pathNumber];
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

                Upgrade upgrade = path[upgradeProgress[pathNumber] - 1];
                recache(upgrade);

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
                    this.rangeModifier = getModifier(activeEffects);
                    break;
                }
                case FIRE_RATE: {
                    this.fireRateModifier = getModifier(activeEffects);
                    break;
                }
                case PROJECTILE_SPEED: {
                    this.projectileSpeedModifier = getModifier(activeEffects);
                    break;
                }
                case PROJECTILE_DAMAGE: {
                    this.projectileDamageModifier = getModifier(activeEffects);
                    break;
                }
                case PROJECTILE: {
                    this.projectileTypeModifier = (Projectile.Type) effect.value;
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
        return type.range * rangeModifier;
    }

    public float getFireRate() {
        return type.fireRate * fireRateModifier;
    }

    public float getProjectileSpeed() {
        return type.projectiveSpeed * projectileSpeedModifier;
    }

    public float getProjectileDamage() {
        return 1.0f * projectileDamageModifier;
    }

    public Projectile.Type getProjectileType() {
        return projectileTypeModifier != null ? projectileTypeModifier : type.projectileType;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        upgradeData = UpgradeReader.get(type);
    }
}
