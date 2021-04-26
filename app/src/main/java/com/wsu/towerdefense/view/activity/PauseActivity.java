package com.wsu.towerdefense.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import com.wsu.towerdefense.audio.AdvancedSoundPlayer;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;
import com.wsu.towerdefense.Util;
import com.wsu.towerdefense.audio.Music;

public class PauseActivity extends Activity {

    private AdvancedSoundPlayer audioButtonPress;

    public static boolean rerender = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_pause);
        onWindowFocusChanged(true);

        audioButtonPress = new AdvancedSoundPlayer(R.raw.ui_button_press);
    }

    public void btnPauseSettingsOnClick(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        startActivity(new Intent(PauseActivity.this, SettingsActivity.class));
    }

    public void btnResumeOnClick(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        finishAfterTransition();   // finishes pause activity and resumes game activity
    }

    public void btnExitOnClick(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        Music.getInstance(this).playMenu();

        // Close Game and go back to game selection
        Intent intent = new Intent().setClass(PauseActivity.this, MainActivity.class);
        startActivity(intent);
        finishAffinity();
        Log.i(getString(R.string.logcatKey), "Exiting game");
    }

    @Override
    public void onResume() {
        super.onResume();
        rerender = true;
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
