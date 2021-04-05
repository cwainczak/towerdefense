package com.wsu.towerdefense.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.wsu.towerdefense.Highscores.DBTools;
import com.wsu.towerdefense.R;

public class UpdateScoresActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_update_scores);
        onWindowFocusChanged(true);
    }

    public void btnOkayOnClick(View view) {
        new DBTools().execute();
        finish();
    }

}



