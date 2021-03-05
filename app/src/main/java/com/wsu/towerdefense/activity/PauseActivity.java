package com.wsu.towerdefense.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.wsu.towerdefense.R;

public class PauseActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_pause);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int fullscreenWidth = dm.widthPixels; // Width of the default display
        int fullscreenHeight = dm.heightPixels;   // Height of the default display

        float sizeRatio = 0.7f;    // this window is 70% of the default landscape window size

        int windowWidth = (int) (fullscreenWidth * sizeRatio);  // width of the pop-up window
        int windowHeight = (int) (fullscreenHeight * sizeRatio);    // height of the pop-up window

        getWindow().setLayout(windowWidth, windowHeight);

        // initialize and resize all buttons
        Button btn_resume = findViewById(R.id.btn_resume);
        resizeButton(btn_resume, sizeRatio);

        Button btn_pause_settings = findViewById(R.id.btn_pause_settings);
        resizeButton(btn_pause_settings, sizeRatio);

        Button btn_exit = findViewById(R.id.btn_exit);
        resizeButton(btn_exit, sizeRatio);
    }

    public void btnPauseSettingsOnClick(View view){
        startActivity(new Intent(PauseActivity.this, SettingsActivity.class));
    }

    public void btnResumeOnClick(View view){
        finish();   // finishes pause activity and resumes game activity
    }

    public void btnExitOnClick(View view){
        // Close Game and go back to game selection
        Intent intent = new Intent().setClass(PauseActivity.this, GameSelectionActivity.class);
        startActivity(intent);
        finishAffinity();
        Log.i(getString(R.string.logcatKey), "Exiting game and returning to Game Select Menu.");
    }

    private void resizeButton(Button b, float ratio){
        int newWidth = (int) (b.getWidth() * ratio);
        int newHeight = (int) (b.getHeight() * ratio);
        b.setWidth(newWidth);
        b.setHeight(newHeight);
    }

}
