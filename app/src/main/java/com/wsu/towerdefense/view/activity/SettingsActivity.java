package com.wsu.towerdefense.view.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.wsu.towerdefense.audio.AdvancedSoundPlayer;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;
import com.wsu.towerdefense.Util;
import com.wsu.towerdefense.audio.Music;

public class SettingsActivity extends AppCompatActivity {

    private AdvancedSoundPlayer audioButtonPress;

    private SeekBar sb_music;
    private SeekBar sb_soundFx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        onWindowFocusChanged(true);

        audioButtonPress = new AdvancedSoundPlayer(R.raw.ui_button_press);

        sb_music = findViewById(R.id.sb_music);
        sb_soundFx = findViewById(R.id.sb_soundFx);

        SharedPreferences pref = getSharedPreferences(getString(R.string.pref_file_key),
            Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        sb_music.setProgress((int) pref.getLong(
            getString(R.string.pref_key_music_volume),
            getResources().getInteger(R.integer.pref_def_volume)
        ));

        sb_soundFx.setProgress((int) pref.getLong(
            getString(R.string.pref_key_sfx_volume),
            getResources().getInteger(R.integer.pref_def_volume)
        ));

        sb_music.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putLong(getString(R.string.pref_key_music_volume), progress);
                editor.apply();

                Music.getInstance(seekBar.getContext()).updateVolume(seekBar.getContext());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_soundFx.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putLong(getString(R.string.pref_key_sfx_volume), progress);
                editor.apply();

                audioButtonPress.play(
                    seekBar.getContext(),
                    Settings.getSFXVolume(seekBar.getContext())
                );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * This method is for when the back button is clicked. When the back button is clicked, it goes
     * to the MainActivity.
     *
     * @param view view
     */
    public void btnBackClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

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
            Util.hideNavigator(getWindow());
        }
    }
}
