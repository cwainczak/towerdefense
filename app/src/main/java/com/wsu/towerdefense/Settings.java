package com.wsu.towerdefense;

/**
 * The Settings class holds global enums representing the game's current state.
 */
public abstract class Settings {

    public enum GameMode {
        NORMAL,
        DEBUG
    }

    /**
     * The mode the game is currently running in.
     */
    public static final GameMode gameMode = GameMode.NORMAL;
}
