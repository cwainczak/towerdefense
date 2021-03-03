package com.wsu.towerdefense.activity;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import com.wsu.towerdefense.Game;
import com.wsu.towerdefense.R;

public class GameActivity extends AppCompatActivity {

    Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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