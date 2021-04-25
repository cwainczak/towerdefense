package com.wsu.towerdefense.view.activity;

import android.os.Bundle;
import android.view.View;

import androidx.gridlayout.widget.GridLayout;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wsu.towerdefense.Controller.audio.AdvancedSoundPlayer;
import com.wsu.towerdefense.Model.Highscores.DBTools;
import com.wsu.towerdefense.Model.Highscores.HighScore;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScoresActivity extends AppCompatActivity {

    private AdvancedSoundPlayer audioButtonPress;
    private GridLayout grd_scores;
    private TextView txt_loadingScores;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        onWindowFocusChanged(true);

        audioButtonPress = new AdvancedSoundPlayer(R.raw.ui_button_press);

        grd_scores = findViewById(R.id.grd_scores);
        txt_loadingScores = findViewById(R.id.txt_loadingScores);

        List<TextView> txt_names = Arrays.asList(
                findViewById(R.id.txt_name1),
                findViewById(R.id.txt_name2),
                findViewById(R.id.txt_name3),
                findViewById(R.id.txt_name4),
                findViewById(R.id.txt_name5)
        );
        List<TextView> txt_scores = Arrays.asList(
                findViewById(R.id.txt_score1),
                findViewById(R.id.txt_score2),
                findViewById(R.id.txt_score3),
                findViewById(R.id.txt_score4),
                findViewById(R.id.txt_score5)
        );


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

                // removing the loading scores text view and displaying the gridlayout of scores
                txt_loadingScores.setVisibility(View.INVISIBLE);
                grd_scores.setVisibility(View.VISIBLE);

                for (int i = 0; i < highScores.size(); i++) {
                    txt_names.get(i).setText(highScores.get(i).getName());
                    txt_scores.get(i).setText(String.valueOf(highScores.get(i).getScore()));
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
