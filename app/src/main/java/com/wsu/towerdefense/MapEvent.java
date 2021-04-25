package com.wsu.towerdefense;

import com.wsu.towerdefense.Model.Enemy;
import com.wsu.towerdefense.Model.tower.Tower;

public abstract class MapEvent {

    public static class PlaceTower extends MapEvent {

        public final Tower tower;

        public PlaceTower(Tower tower) {
            this.tower = tower;
        }
    }

    public static class RemoveTower extends MapEvent {

    }

    public static class SpawnEnemy extends MapEvent {

        public final Enemy enemy;

        public SpawnEnemy(Enemy enemy) {
            this.enemy = enemy;
        }
    }
}
