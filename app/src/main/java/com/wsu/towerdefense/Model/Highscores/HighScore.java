package com.wsu.towerdefense.Model.Highscores;

public class HighScore {

    private String name;
    private int score;

    public HighScore(String name, int score) {
        this.setName(name);
        this.setScore(score);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

}
