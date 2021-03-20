package com.wsu.towerdefense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.wsu.towerdefense.Application;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.save.SaveState;
import com.wsu.towerdefense.save.Serializer;
import java.io.IOException;

public class GameSelectionActivity extends AppCompatActivity {

    private SaveState saveState = null;

    private Button btn_resume;
    private Button btn_delete;
    private ImageButton imgBtn_back;
    private ImageButton imgBtn_settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_selection);
        onWindowFocusChanged(true);

        btn_resume = findViewById(R.id.resumeGame);
        btn_delete = findViewById(R.id.deleteGame);

        // load save game if it exists
        boolean hasSave = Serializer.exists(Application.context, Serializer.SAVEFILE);

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
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("saveState", saveState);
        startActivity(intent);
    }

    public void btnDeleteGameClicked(View view) {
        if (Serializer.exists(Application.context, Serializer.SAVEFILE)) {
            Serializer.delete(Application.context, Serializer.SAVEFILE);

            btn_resume.setEnabled(false);
            btn_delete.setEnabled(false);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ActivityUtil.hideNavigator(getWindow());
        }
    }

    // testing
    public void btnBackClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void btnSettingsClicked(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
