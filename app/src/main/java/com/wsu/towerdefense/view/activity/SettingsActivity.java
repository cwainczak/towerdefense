package com.wsu.towerdefense.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.audio.AdvancedSoundPlayer;

public class SettingsActivity extends AppCompatActivity {

    private AdvancedSoundPlayer audioButtonPress;

    TextView tv_versionNumber;
    SeekBar sb_music;
    SeekBar sb_soundFx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        onWindowFocusChanged(true);

        audioButtonPress = new AdvancedSoundPlayer(R.raw.ui_button_press);

        tv_versionNumber = findViewById(R.id.tv_versionNumber);

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
        audioButtonPress.play(view.getContext());

        finish();
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
            ActivityUtil.hideNavigator(getWindow());
        }
    }
}
