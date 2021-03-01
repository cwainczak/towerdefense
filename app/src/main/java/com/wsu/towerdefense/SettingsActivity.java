package com.wsu.towerdefense;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.SeekBar;

public class SettingsActivity extends AppCompatActivity {

    SeekBar sb_music;
    SeekBar sb_soundFx;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sb_music = findViewById(R.id.sb_music);
        sb_soundFx = findViewById(R.id.sb_soundFx);

        sb_music.setProgress(100);
        sb_soundFx.setProgress(100);
    }
}