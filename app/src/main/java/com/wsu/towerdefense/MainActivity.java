package com.wsu.towerdefense;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // go to game activity until menu is made
        //Intent intent = new Intent(this, GameActivity.class);
        //startActivity(intent);
    }
}