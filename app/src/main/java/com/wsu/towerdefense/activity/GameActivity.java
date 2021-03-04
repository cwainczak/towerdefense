package com.wsu.towerdefense.activity;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import com.wsu.towerdefense.Application;
import com.wsu.towerdefense.Game;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.save.SaveState;

public class GameActivity extends AppCompatActivity {

    ConstraintLayout cl_gameLayout;
    ConstraintLayout cl_towerLayout;

    Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        cl_gameLayout = findViewById(R.id.cl_gameLayout);
        cl_towerLayout = findViewById(R.id.cl_towerLayout);


        // display size
        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);

        // save state
        SaveState saveState = (SaveState) getIntent().getSerializableExtra("saveState");

        try {
            game = new Game(this, displaySize.x, displaySize.y, saveState);
        } catch (Exception e) {
            // redirect game errors to logcat
            Log.e(getString(R.string.logcatKey), Log.getStackTraceString(e));
        }

        cl_gameLayout.addView(game);

    }
}