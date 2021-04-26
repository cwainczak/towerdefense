package com.wsu.towerdefense.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.wsu.towerdefense.audio.AdvancedSoundPlayer;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;
import com.wsu.towerdefense.Util;
import com.wsu.towerdefense.audio.Music;

public class MainActivity extends AppCompatActivity {

    private AdvancedSoundPlayer audioButtonPress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onWindowFocusChanged(true);

        audioButtonPress = new AdvancedSoundPlayer(R.raw.ui_button_press);

        Music.getInstance(this).playMenu();
    }

    /**
     * This method is for when the start button is clicked. When the start button is clicked, it
     * goes to the MapSelectionActivity.
     *
     * @param view view
     */
    public void btnStartClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        Intent intent = new Intent(this, GameSelectionActivity.class);
        startActivity(intent);
    }

    /**
     * This method is for when the scores button is clicked. When the scores button is clicked, it
     * goes to the ScoresActivity.
     *
     * @param view view
     */
    public void btnScoresClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        Intent intent = new Intent(this, ScoresActivity.class);
        startActivity(intent);
    }

    /**
     * This method is for when the settings button is clicked. When the settings button is clicked,
     * it goes to the SettingsActivity.
     *
     * @param view view
     */
    public void btnSettingsClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioButtonPress.release();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Util.hideNavigator(getWindow());
        }
    }
}
