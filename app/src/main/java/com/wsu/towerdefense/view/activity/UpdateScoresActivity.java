package com.wsu.towerdefense.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.mysql.jdbc.StringUtils;
import com.wsu.towerdefense.Controller.audio.Music;
import com.wsu.towerdefense.Model.Highscores.DBTools;
import com.wsu.towerdefense.R;

import com.wsu.towerdefense.Util;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifImageView;

public class UpdateScoresActivity extends Activity {

    private String playerUsername;
    private int playerScore;

    private final int MAX_USERNAME_LENGTH = 25;

    private enum ErrorType {
        BLANK,
        OVER_CAPACITY
    }

    private TextView txv_error_msg;
    private ImageView img_error_symbol;
    private EditText textField;
    private TextView scoreDisplayer;
    private ImageView tint;
    private Button submitButton;
    private GifImageView gifImageView;
    private ImageButton xBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_update_scores);
        onWindowFocusChanged(true);

        txv_error_msg = findViewById(R.id.txv_error_msg);
        img_error_symbol = findViewById(R.id.img_error_symbol);
        textField = findViewById(R.id.plt_username);
        scoreDisplayer = findViewById(R.id.txv_score);
        tint = findViewById(R.id.img_tint);
        submitButton = findViewById(R.id.btn_submit);
        gifImageView = findViewById(R.id.gifImageView);
        xBtn = findViewById(R.id.btn_x);
        xBtn.setImageResource(R.mipmap.x_button);

        this.playerScore = getIntent().getIntExtra("score", 0);
        boolean hasWon = getIntent().getBooleanExtra("won", false);

        scoreDisplayer.setText(scoreDisplayer.getText() + " " + this.playerScore);

        displayWinOrLossGIF(true, hasWon);
        Timer overlayTimer = new Timer();
        overlayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> displayWinOrLossGIF(false, hasWon));
            }
        }, 7000);
    }

    public void btnSubmitOnClick(View view) {
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

    public void btnX(View view){
        displayWinOrLossGIF(false, false);
    }

    public void displayError(ErrorType errorType) {
        img_error_symbol.setBackgroundResource(R.mipmap.error_symbol);

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

    private void displayWinOrLossGIF(boolean display, boolean hasWon) {
        tint.setVisibility(display ? View.VISIBLE : View.INVISIBLE);
        submitButton.setVisibility(display ? View.INVISIBLE : View.VISIBLE);
        xBtn.setVisibility(display ? View.VISIBLE : View.INVISIBLE);

        // when you win or you lose should be displayed
        if (display) {
            if (hasWon) {
                Music.getInstance(this).playWin(this);
            } else {
                Music.getInstance(this).playLose(this);
            }

            gifImageView.setBackgroundResource(
                hasWon ? R.drawable.you_win : R.drawable.you_lose_pickle
            );

        }
        // when you win or you lose should not be displayed
        else {
            gifImageView.setVisibility(View.INVISIBLE);
            Music.getInstance(this).stopAll(this);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Util.hideNavigator(getWindow());
        }
    }
}
