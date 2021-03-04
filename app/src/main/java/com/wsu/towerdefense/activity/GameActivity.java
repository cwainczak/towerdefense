package com.wsu.towerdefense.activity;

import android.content.Intent;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;

import com.wsu.towerdefense.Application;
import com.wsu.towerdefense.Game;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.save.SaveState;

public class GameActivity extends AppCompatActivity {

    ConstraintLayout gameLayout;
    Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

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

        gameLayout = findViewById(R.id.gameLayout);
        ViewGroup.LayoutParams gameViewLayoutParams = findViewById(R.id.gameView).getLayoutParams();
        gameLayout.removeView(findViewById(R.id.gameView));
        gameLayout.addView(game, gameViewLayoutParams);
    }


    public void gameOver(){

        // Go back to game selection
        Intent intent = new Intent().setClass(this, GameSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Log.i(getString(R.string.logcatKey),"Game Over. Returning to Game Select Menu.");
    }


}