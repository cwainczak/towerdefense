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
     * This method is for when the start button is clicked. When the start button is clicked, it
     * goes to the MapSelectionActivity.
     *
     * @param view view
     */
    public void btnStartClicked(View view) {
        Intent intent = new Intent(this, GameSelectionActivity.class);
        startActivity(intent);
    }

}
