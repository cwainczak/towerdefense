package com.wsu.towerdefense;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class MapSelectionActivity extends AppCompatActivity {

    ImageView map_1;
    ImageView map_2;
    ImageView map_3;
    ImageView map_4;
    ImageView map_5;
    ImageView map_6;
    ImageView map_7;
    ImageView map_8;
    ImageView map_9;

    List<ImageView> mapList;

    TextView txt_mapName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_selection);

        map_1 = findViewById(R.id.img_Map1);
        map_2 = findViewById(R.id.img_Map2);
        map_3 = findViewById(R.id.img_Map3);
        map_4 = findViewById(R.id.img_Map4);
        map_5 = findViewById(R.id.img_Map5);
        map_6 = findViewById(R.id.img_Map6);
        map_7 = findViewById(R.id.img_Map7);
        map_8 = findViewById(R.id.img_Map8);
        map_9 = findViewById(R.id.img_Map9);
        mapList = Arrays.asList(map_1, map_2, map_3, map_4, map_5, map_6, map_7, map_8, map_9);

        txt_mapName = findViewById(R.id.txt_mapName);
        txt_mapName.setVisibility(View.INVISIBLE);
    }


    /**
     * This method is for when the play button is clicked. When the play button clicked, it
     * goes to the GameActivity.
     *
     * @param view view
     */
    public void btnPlayClicked(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }


    /**
     * This method is for when the back button is clicked. When the back button clicked, it
     * goes to the MainActivity.
     *
     * @param view view
     */
    public void btnBackClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    /**
     * This method is for when one of the ImageView objects representing game maps is clicked.
     * It sets the text of the txt_mapName to the map name and sets its visibility to visible.
     *
     * @param view view
     */
    public void mapSelected(View view) {
        for (int i = 0; i < mapList.size(); i++) {
            if (mapList.get(i).isPressed()) {
                txt_mapName.setText("Map " + (i + 1));
                txt_mapName.setVisibility(View.VISIBLE);
            }
        }
    }

}
