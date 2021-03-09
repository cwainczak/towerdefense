package com.wsu.towerdefense.save;

import com.wsu.towerdefense.Game;
import com.wsu.towerdefense.Tower;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class SaveState implements Serializable {

    public final String saveFile;

    // save data
    public final String mapName;
    public final List<Tower> towers;
    public final int lives;
    public int money;

    public SaveState(String saveFile, Game game) {
        this.saveFile = saveFile;

        this.mapName = game.getMap().getName();

        this.lives = game.getLives();

        this.money = game.money;

        // TODO: save only relevant tower data
        this.towers = game.getTowers();

    }

    @Override
    public String toString() {
        return "SaveState{" +
            "saveFile='" + saveFile + '\'' +
            ", mapName='" + mapName + '\'' +
            ", lives='" + lives + '\'' +
            ", money='" + money + '\'' +
            ", towers=" + (towers.stream().map(Object::toString)
            .collect(Collectors.joining(", "))) +
            '}';
    }
}
