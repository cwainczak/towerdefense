package com.wsu.towerdefense.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wsu.towerdefense.Model.Game.Difficulty;
import com.wsu.towerdefense.audio.AdvancedSoundPlayer;
import com.wsu.towerdefense.Model.Highscores.DBTools;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;


import com.wsu.towerdefense.Util;

import java.sql.SQLException;

public class ScoresActivity extends AppCompatActivity {

    private AdvancedSoundPlayer audioButtonPress;
    private TextView txt_loadingScores;
    private TableLayout tbl_scores;
    private Button btn_easyScores;
    private Button btn_mediumScores;
    private Button btn_hardScores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        onWindowFocusChanged(true);

        audioButtonPress = new AdvancedSoundPlayer(R.raw.ui_button_press);

        txt_loadingScores = findViewById(R.id.txt_loadingScores);
        tbl_scores = findViewById(R.id.tbl_scores);

        btn_easyScores = findViewById(R.id.btn_easyScores);
        btn_mediumScores = findViewById(R.id.btn_mediumScores);
        btn_hardScores = findViewById(R.id.btn_hardScores);

        // OnClickListeners for the buttons
        btn_easyScores.setOnClickListener(view -> {
            audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));
            changeDifficulty(Difficulty.EASY);
        });

        btn_mediumScores.setOnClickListener(view -> {
            audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));
            changeDifficulty(Difficulty.MEDIUM);
        });

        btn_hardScores.setOnClickListener(view -> {
            audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));
            changeDifficulty(Difficulty.HARD);
        });

        changeDifficulty(Difficulty.EASY);
    }

    private void changeDifficulty(Difficulty difficulty) {
        btn_easyScores.setTextColor(
            difficulty == Difficulty.EASY
                ? getColor(android.R.color.white)
                : getColor(R.color.not_selected_text)
        );
        btn_mediumScores.setTextColor(
            difficulty == Difficulty.MEDIUM
                ? getColor(android.R.color.white)
                : getColor(R.color.not_selected_text)
        );
        btn_hardScores.setTextColor(
            difficulty == Difficulty.HARD
                ? getColor(android.R.color.white)
                : getColor(R.color.not_selected_text)
        );

        tbl_scores.removeAllViews();
        DBTools.tableName = getTableName(difficulty);
        txt_loadingScores.setVisibility(View.VISIBLE);
        getScores();
    }

    private static String getTableName(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
            default:
                return "EASY";
            case MEDIUM:
                return "MEDIUM";
            case HARD:
                return "HARD";
        }
    }

    /**
     * This method displays the corresponding high scores in the table.
     */
    private void getScores() {
        DBTools dbt = new DBTools(rs -> {
            try {
                // removing the loading scores text view and displaying the scores
                txt_loadingScores.setVisibility(View.INVISIBLE);

                while (rs.next()) {
                    String name = rs.getString(1);
                    String score = rs.getString(2);

                    TextView txt_name = new TextView(this);
                    txt_name.setText(name);
                    txt_name.setTextColor(Color.WHITE);
                    txt_name.setTextSize(18);
                    txt_name.setShadowLayer(5, 5, 5, Color.BLACK);

                    TextView txt_score = new TextView(this);
                    txt_score.setText(score);
                    txt_score.setTextColor(Color.WHITE);
                    txt_score.setTextSize(18);
                    txt_name.setShadowLayer(5, 5, 5, Color.BLACK);

                    // empty space
                    TextView txt_empty = new TextView(this);
                    txt_empty.setText("                           ");

                    TableRow tableRow = new TableRow(this);
                    tableRow.setPadding(0, 20, 0, 20);
                    tableRow.addView(txt_name);
                    tableRow.addView(txt_empty);
                    tableRow.addView(txt_score);

                    tableRow.setGravity(Gravity.CENTER);
                    tbl_scores.addView(tableRow);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        dbt.execute();
    }

    /**
     * This method is for when the back button is clicked. When the back button is clicked, it goes
     * to the MainActivity.
     *
     * @param view view
     */
    public void btnBackClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioButtonPress.release();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Util.hideNavigator(getWindow());
        }
    }
}
