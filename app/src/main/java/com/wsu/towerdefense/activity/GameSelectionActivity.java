package com.wsu.towerdefense.activity;

import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.wsu.towerdefense.Application;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.save.SaveState;
import com.wsu.towerdefense.save.Serializer;
import java.io.IOException;

public class GameSelectionActivity extends AppCompatActivity {

    private SaveState saveState = null;

    private Button resumeButton;
    private Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_selection);
        onWindowFocusChanged(true);

        resumeButton = findViewById(R.id.resumeGame);
        deleteButton = findViewById(R.id.deleteGame);

        // load save game if it exists
        boolean hasSave = Serializer.exists(Application.context, Serializer.SAVEFILE);

        // disable buttons when no save exists
        resumeButton.setEnabled(hasSave);
        deleteButton.setEnabled(hasSave);

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
        Intent intent = new Intent(this, MapSelectionActivity.class);
        startActivity(intent);
    }

    /**
     * This method is for when the nee resume game button is clicked. When the resume game button is
     * clicked, it goes to the GameActivity.
     *
     * @param view view
     */
    public void btnResumeGameClicked(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("saveState", saveState);
        startActivity(intent);
    }

    public void btnDeleteGameClicked(View view) {
        if (Serializer.exists(Application.context, Serializer.SAVEFILE)) {
            Serializer.delete(Application.context, Serializer.SAVEFILE);

            resumeButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ActivityUtil.hideNavigator(getWindow());
        }
    }
}
