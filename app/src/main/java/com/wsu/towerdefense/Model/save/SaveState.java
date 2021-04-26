package com.wsu.towerdefense.Model.save;

import com.wsu.towerdefense.Model.Waves;
import com.wsu.towerdefense.Model.tower.Tower;
import com.wsu.towerdefense.Model.Game;
import com.wsu.towerdefense.Model.Game.Difficulty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaveState implements Serializable {

    public final String saveFile;
    public final LocalDateTime date;

    // save data
    public final String mapName;
    public final List<Tower> towers;
    public final int lives;
    public final int money;
    public final int score;
    public final Waves waves;
    public final Difficulty difficulty;

    public SaveState(String saveFile, LocalDateTime date, Game game) {
        this.saveFile = saveFile;
        this.date = date;

        this.mapName = game.getMap().getName();
        this.towers = new ArrayList<>(game.getTowers());
        this.lives = game.getLives();
        this.money = game.getMoney();
        this.score = game.getScore();
        this.waves = game.getWaves();
        this.difficulty = game.getDifficulty();
    }
}
