package com.wsu.towerdefense.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wsu.towerdefense.Controller.audio.AdvancedSoundPlayer;
import com.wsu.towerdefense.Model.Highscores.DBTools;
import com.wsu.towerdefense.Model.Highscores.HighScore;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;

import java.sql.SQLException;
import java.util.ArrayList;

public class ScoresActivity extends AppCompatActivity {

    private AdvancedSoundPlayer audioButtonPress;
    private TextView txt_loadingScores;
    private LinearLayout scoresContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        onWindowFocusChanged(true);

        audioButtonPress = new AdvancedSoundPlayer(R.raw.ui_button_press);

        txt_loadingScores = findViewById(R.id.txt_loadingScores);
        scoresContainer = findViewById(R.id.scoresContainer);


        DBTools dbt = new DBTools(rs -> {
            try {
                ArrayList<HighScore> highScores = new ArrayList<>();
                while (rs.next()) {
                    String name = rs.getString(1);
                    int score = rs.getInt(2);

                    // create new HighScore object and add it to an arrayList
                    HighScore hs = new HighScore(name, score);
                    highScores.add(hs);
                }

                // removing the loading scores text view and displaying the scores
                txt_loadingScores.setVisibility(View.INVISIBLE);

                for (int i = 0; i < highScores.size(); i++) {

                    // new textView to hold the name and score
                    TextView nameAndScore = new TextView(this);
                    nameAndScore.setText(highScores.get(i).getName() + " " + highScores.get(i).getScore());
                    nameAndScore.setTextColor(Color.BLACK);
                    nameAndScore.setTextSize(18);
                    scoresContainer.addView(nameAndScore);

                    // empty textView to used as spaces between rows
                    TextView emptySpace = new TextView(this);
                    emptySpace.setText("");
                    emptySpace.setTextSize(5);
                    scoresContainer.addView(emptySpace);
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
            ActivityUtil.hideNavigator(getWindow());
        }
    }
}
