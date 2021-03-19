package com.wsu.towerdefense.upgrade;

import java.util.Arrays;

/**
 * Combined upgraded paths that a {@link com.wsu.towerdefense.Tower.Type Tower.Type} can take
 */
public class TowerUpgradeData {

    public final Upgrade<?>[] path1;
    public final Upgrade<?>[] path2;

    public TowerUpgradeData(Upgrade<?>[] path1, Upgrade<?>[] path2) {
        this.path1 = path1;
        this.path2 = path2;
    }

    @Override
    public String toString() {
        return "TowerUpgrades{" +
            "path1=" + Arrays.toString(path1) +
            ", path2=" + Arrays.toString(path2) +
            '}';
    }
}
