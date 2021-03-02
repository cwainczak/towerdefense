package com.wsu.towerdefense;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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


    /**
     * This method is for when the back button is clicked. When the back button is clicked, it
     * goes to the MainActivity.
     *
     * @param view view
     */
    public void btnBackClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
