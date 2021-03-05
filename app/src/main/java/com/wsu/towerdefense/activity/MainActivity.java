package com.wsu.towerdefense.activity;

import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.wsu.towerdefense.R;
import com.wsu.towerdefense.map.MapReader;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onWindowFocusChanged(true);
    }

    /**
     * This method is for when the start button is clicked. When the start button is clicked, it
     * goes to the MapSelectionActivity.
     *
     * @param view view
     */
    public void btnStartClicked(View view) {
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
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }
}
