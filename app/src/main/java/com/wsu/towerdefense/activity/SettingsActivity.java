package com.wsu.towerdefense.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wsu.towerdefense.R;

public class SettingsActivity extends AppCompatActivity {

    TextView tv_versionNumber;
    SeekBar sb_music;
    SeekBar sb_soundFx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        onWindowFocusChanged(true);

        tv_versionNumber = findViewById(R.id.tv_versionNumber);
        tv_versionNumber.setText("Version 1.0");

        sb_music = findViewById(R.id.sb_music);
        sb_soundFx = findViewById(R.id.sb_soundFx);

        sb_music.setProgress(100);
        sb_soundFx.setProgress(100);
    }

    /**
     * This method is for when the back button is clicked. When the back button is clicked, it goes
     * to the MainActivity.
     *
     * @param view view
     */
    public void btnBackClicked(View view) {
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ActivityUtil.hideNavigator(getWindow());
        }
    }
}
