package com.wsu.towerdefense.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;
import com.wsu.towerdefense.audio.AdvancedSoundPlayer;
import com.wsu.towerdefense.save.SaveState;
import com.wsu.towerdefense.save.Serializer;
import java.io.IOException;

public class GameSelectionActivity extends AppCompatActivity {

    private AdvancedSoundPlayer audioButtonPress;

    private SaveState saveState = null;

    private Button btn_resume;
    private Button btn_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_selection);
        onWindowFocusChanged(true);

        audioButtonPress = new AdvancedSoundPlayer(R.raw.ui_button_press);

        btn_resume = findViewById(R.id.resumeGame);
        btn_delete = findViewById(R.id.deleteGame);

        // load save game if it exists
        boolean hasSave = Serializer.exists(GameSelectionActivity.this, Serializer.SAVEFILE);

        // disable buttons when no save exists
        btn_resume.setEnabled(hasSave);
        btn_delete.setEnabled(hasSave);

        if (hasSave) {
            try {
                saveState = Serializer.load(this, Serializer.SAVEFILE);
            } catch (IOException | ClassNotFoundException e) {
                Log.e(getString(R.string.logcatKey), "Error while loading save file", e);
            }
        }
    }

    /**
     * This method is for when the new game button is clicked. When the new game button is clicked,
     * it goes to the MapSelectionActivity.
     *
     * @param view view
     */
    public void btnNewGameClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        Intent intent = new Intent(this, MapSelectionActivity.class);
        startActivity(intent);
    }

    /**
     * This method is for when the resume game button is clicked. When the resume game button is
     * clicked, it goes to the GameActivity.
     *
     * @param view view
     */
    public void btnResumeGameClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("saveState", saveState);
        startActivity(intent);
    }

    public void btnDeleteGameClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        if (Serializer.exists(GameSelectionActivity.this, Serializer.SAVEFILE)) {
            Serializer.delete(GameSelectionActivity.this, Serializer.SAVEFILE);

            btn_resume.setEnabled(false);
            btn_delete.setEnabled(false);
        }
    }

    // testing
    public void btnBackClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        finish();
    }


    public void btnSettingsClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ActivityUtil.hideNavigator(getWindow());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioButtonPress.release();
    }
}
