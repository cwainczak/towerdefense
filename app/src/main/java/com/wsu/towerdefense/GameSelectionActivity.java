package com.wsu.towerdefense;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class GameSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_selection);
    }


    /**
     * This method is for when the new game button is clicked. When the new game button is clicked,
     * it goes to the MapSelectionActivity.
     *
     * @param view view
     */
    public void btnNewGameClicked(View view) {
        Intent intent = new Intent(this, MapSelectionActivity.class);
        startActivity(intent);
    }


    /**
     * This method is for when the nee resume game button is clicked. When the resume game button
     * is clicked, it goes to the GameActivity.
     *
     * @param view view
     */
    public void btnResumeGameClicked(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

}
