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

    public SaveState(String saveFile, Game game) {
        this.saveFile = saveFile;

        this.mapName = game.getMap().getName();
        // TODO: save only relevant tower data
        this.towers = game.getTowers();
    }

    @Override
    public String toString() {
        return "SaveState{" +
            "saveFile='" + saveFile + '\'' +
            ", mapName='" + mapName + '\'' +
            ", towers=" + (
            towers.stream().map(Object::toString).collect(Collectors.joining(", "))
        ) +
            '}';
    }
}
