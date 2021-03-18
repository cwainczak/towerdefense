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

    public static boolean rerender = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_pause);
        onWindowFocusChanged(true);
    }

    public void btnPauseSettingsOnClick(View view) {
        startActivity(new Intent(PauseActivity.this, SettingsActivity.class));
    }

    public void btnResumeOnClick(View view) {
        finishAfterTransition();   // finishes pause activity and resumes game activity
    }

    public void btnExitOnClick(View view) {
        // Close Game and go back to game selection
        Intent intent = new Intent().setClass(PauseActivity.this, GameSelectionActivity.class);
        startActivity(intent);
        finishAffinity();
        Log.i(getString(R.string.logcatKey), "Exiting game");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ActivityUtil.hideNavigator(getWindow());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        rerender = true;
    }
}
