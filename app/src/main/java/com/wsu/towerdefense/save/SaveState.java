package com.wsu.towerdefense.save;

import com.wsu.towerdefense.Game;
import com.wsu.towerdefense.Tower;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SaveState implements Serializable {

    public final String saveFile;

    // save data
    public final String mapName;
    public final List<Tower> towers;
    public final int lives;
    public final int money;

    public SaveState(String saveFile, Game game) {
        this.saveFile = saveFile;

        this.mapName = game.getMap().getName();
        this.towers = new ArrayList<>(game.getTowers());
        this.lives = game.getLives();
        this.money = game.getMoney();
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
