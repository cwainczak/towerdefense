package com.wsu.towerdefense;

import android.content.Intent;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.wsu.towerdefense.map.MapReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize maps
        try {
            MapReader.init(this);
        } catch (IOException e) {
            Log.e(getString(R.string.logcatKey), "Error while initializing maps", e);
        }

        // go to game activity until menu is made
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
}