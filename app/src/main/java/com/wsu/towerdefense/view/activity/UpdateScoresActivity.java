package com.wsu.towerdefense.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mysql.jdbc.StringUtils;
import com.wsu.towerdefense.Highscores.DBTools;
import com.wsu.towerdefense.R;

public class UpdateScoresActivity extends Activity {

    private String playerUsername;
    private int playerScore;

    private final int MAX_USERNAME_LENGTH = 25;
    private enum ERROR_TYPE {
        BLANK,
        OVER_CAPACITY
    }

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
        if (StringUtils.isEmptyOrWhitespaceOnly(this.playerUsername)){
            displayError(ERROR_TYPE.BLANK);
            return;
        }
        else if (this.playerUsername.length() > this.MAX_USERNAME_LENGTH){
            displayError(ERROR_TYPE.OVER_CAPACITY);
            return;
        }
        DBTools dbt = new DBTools();
        dbt.initUsernameAndScore(this.playerUsername, this.playerScore);
        dbt.execute();
        finish();
    }

    public void displayError(ERROR_TYPE error_type){
        findViewById(R.id.imv_error_symbol).setBackgroundResource(R.mipmap.error_symbol);
        String errorMessage = "";
        switch (error_type) {
            case BLANK:
                errorMessage = "No username entered";
                break;
            case OVER_CAPACITY:
                errorMessage = "Username too long";
                break;
        }
        ((TextView) findViewById(R.id.txv_error_msg)).setText(errorMessage);
    }

}



