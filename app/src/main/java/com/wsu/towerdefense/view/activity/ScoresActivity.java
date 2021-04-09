package com.wsu.towerdefense.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wsu.towerdefense.Highscores.DBTools;
import com.wsu.towerdefense.Highscores.HighScore;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;
import com.wsu.towerdefense.audio.AdvancedSoundPlayer;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScoresActivity extends AppCompatActivity {

    private AdvancedSoundPlayer audioButtonPress;

    TextView txt_name1;
    TextView txt_name2;
    TextView txt_name3;
    TextView txt_name4;
    TextView txt_name5;
    TextView txt_score1;
    TextView txt_score2;
    TextView txt_score3;
    TextView txt_score4;
    TextView txt_score5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        onWindowFocusChanged(true);

        audioButtonPress = new AdvancedSoundPlayer(R.raw.ui_button_press);

        txt_name1 = findViewById(R.id.txt_name1);
        txt_name2 = findViewById(R.id.txt_name2);
        txt_name3 = findViewById(R.id.txt_name3);
        txt_name4 = findViewById(R.id.txt_name4);
        txt_name5 = findViewById(R.id.txt_name5);

        txt_score1 = findViewById(R.id.txt_score1);
        txt_score2 = findViewById(R.id.txt_score2);
        txt_score3 = findViewById(R.id.txt_score3);
        txt_score4 = findViewById(R.id.txt_score4);
        txt_score5 = findViewById(R.id.txt_score5);

        List<TextView> txt_names = Arrays.asList(txt_name1, txt_name2, txt_name3, txt_name4, txt_name5);
        List<TextView> txt_scores = Arrays.asList(txt_score1, txt_score2, txt_score3, txt_score4, txt_score5);


        DBTools dbt = new DBTools(new OnTaskEnded() {

            @Override
            public void onTaskEnd(ResultSet rs) {
                try {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    ArrayList<HighScore> highScores = new ArrayList<>();
                    while (rs.next()) {
                        String name = rs.getString(1);
                        int score = rs.getInt(2);

                        // create new HighScore object and add it to an arrayList
                        HighScore hs = new HighScore(name, score);
                        highScores.add(hs);
                    }

                    for (int i = 0; i < highScores.size(); i++) {
                        txt_names.get(i).setText(highScores.get(i).getName());
                        txt_scores.get(i).setText(String.valueOf(highScores.get(i).getScore()));
                    }

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }
        });

        dbt.execute();
    }

    public interface OnTaskEnded {

        /**
         * This method is called once the AsyncTask has completed
         */
        void onTaskEnd(ResultSet rs);
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
