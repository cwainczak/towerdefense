package com.wsu.towerdefense.Controller.audio;

import android.content.Context;
import android.media.MediaPlayer;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;
import com.wsu.towerdefense.Util;
import java.io.IOException;

public class Music {

    private static Music instance = null;

    public static Music getInstance(Context context) {
        if (instance == null) {
            instance = new Music(context);
        }
        return instance;
    }

    private static final int menuResID = R.raw.music_menu;
    private static final int gameResID = R.raw.music_game;

    private final MediaPlayer audioMenu;
    private final MediaPlayer audioGame;

    private Music(Context context) {
        this.audioMenu = MediaPlayer.create(context, menuResID);
        this.audioMenu.setLooping(true);

        this.audioGame = MediaPlayer.create(context, gameResID);
        this.audioGame.setLooping(true);
    }

    public void playMenu() {
        this.audioGame.stop();

        this.audioMenu.stop();
        try {
            this.audioMenu.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.audioMenu.start();
    }

    public void playGame() {
        this.audioMenu.stop();

        this.audioGame.stop();
        try {
            this.audioGame.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.audioGame.start();
    }

    public void updateVolume(Context context) {
        float v = Util.adjustVolume(Settings.getMusicVolume(context));
        this.audioMenu.setVolume(v, v);
        this.audioGame.setVolume(v, v);
    }
}
