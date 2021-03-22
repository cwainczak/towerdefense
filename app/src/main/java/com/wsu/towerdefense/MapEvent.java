package com.wsu.towerdefense;

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
