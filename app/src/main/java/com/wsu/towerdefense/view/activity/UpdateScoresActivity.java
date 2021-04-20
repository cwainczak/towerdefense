package com.wsu.towerdefense.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.mysql.jdbc.StringUtils;
import com.wsu.towerdefense.Model.Highscores.DBTools;
import com.wsu.towerdefense.R;

public class UpdateScoresActivity extends Activity {

    private String playerUsername;
    private int playerScore;

    private final int MAX_USERNAME_LENGTH = 25;

    private enum ErrorType {
        BLANK,
        OVER_CAPACITY
    }

    private TextView txv_error_msg;
    private ImageView imv_error_symbol;
    private EditText textField;
    private TextView scoreDisplayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_update_scores);
        onWindowFocusChanged(true);

        txv_error_msg = findViewById(R.id.txv_error_msg);
        imv_error_symbol = findViewById(R.id.imv_error_symbol);
        textField = findViewById(R.id.plt_username);
        scoreDisplayer = findViewById(R.id.txv_score);

        this.playerScore = getIntent().getIntExtra("score", 0);
        scoreDisplayer.setText(scoreDisplayer.getText() + " " + this.playerScore);
    }

    public void btnOkayOnClick(View view) {
        this.playerUsername = textField.getText().toString();
        if (StringUtils.isEmptyOrWhitespaceOnly(this.playerUsername)) {
            displayError(ErrorType.BLANK);
            return;
        } else if (this.playerUsername.length() > this.MAX_USERNAME_LENGTH) {
            displayError(ErrorType.OVER_CAPACITY);
            return;
        }
        DBTools dbt = new DBTools();
        dbt.initUsernameAndScore(this.playerUsername, this.playerScore);
        dbt.execute();
        finishAffinity();
        Intent intent = new Intent(UpdateScoresActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void displayError(ErrorType errorType) {
        imv_error_symbol.setBackgroundResource(R.mipmap.error_symbol);

        String errorMessage = "";
        switch (errorType) {
            case BLANK:
                errorMessage = getString(R.string.blank_username);
                break;
            case OVER_CAPACITY:
                errorMessage = getString(R.string.username_long);
                break;
        }
        txv_error_msg.setText(errorMessage);
    }

}



