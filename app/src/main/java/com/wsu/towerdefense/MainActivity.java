package com.wsu.towerdefense;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    /**
     * This method is for when the play button is clicked.
     *
     * @param view view
     */
    public void playButtonClicked(View view) {
        // go to game activity until menu is made
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

}
