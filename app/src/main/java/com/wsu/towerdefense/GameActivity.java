package com.wsu.towerdefense;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class GameActivity extends AppCompatActivity {

    Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();   // hide the title bar

        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);

        try {
            game = new Game(this, displaySize.x, displaySize.y);
        } catch (Exception e) {
            // redirect game errors to logcat
            Log.e(getString(R.string.logcatKey), Log.getStackTraceString(e));
        }
        setContentView(game);
    }
}