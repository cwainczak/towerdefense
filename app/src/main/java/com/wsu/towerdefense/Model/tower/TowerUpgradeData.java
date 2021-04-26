package com.wsu.towerdefense.Model.tower;

/**
 * Combined upgraded paths that a {@link Tower.Type Tower.Type} can take
 */
public class TowerUpgradeData {

    /**
     * Maximum number of upgrade paths the user can choose before the rest are locked
     */
    public static final int MAX_PATHS = 2;
    /**
     * When an upgrade path reaches this number, other paths cannot be upgraded further
     */
    public static final int LOCK_THRESHOLD = 3;

    public static final int NUM_PATHS = 3;

    public final Upgrade[][] paths;

    public TowerUpgradeData(Upgrade[]... paths) {
        this.paths = paths;
    }
}
