package com.wsu.towerdefense.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.wsu.towerdefense.Highscores.DBTools;
import com.wsu.towerdefense.R;

public class UpdateScoresActivity extends Activity {

    private String playerUsername;
    private int playerScore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_update_scores);
        onWindowFocusChanged(true);
        this.playerScore = getIntent().getIntExtra("score", 0);
        TextView scoreDisplayer = findViewById(R.id.txv_score);
        scoreDisplayer.setText(scoreDisplayer.getText() + " " + this.playerScore);
    }

    public void btnOkayOnClick(View view) {
        EditText textField = findViewById(R.id.plt_username);
        this.playerUsername = textField.getText().toString();
        DBTools dbt = new DBTools();
        dbt.initUsernameAndScore(this.playerUsername, this.playerScore);
        dbt.execute();
        finish();
    }

}



