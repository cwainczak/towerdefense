package com.wsu.towerdefense.activity;

import android.content.Intent;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wsu.towerdefense.Application;
import com.wsu.towerdefense.Game;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.save.SaveState;

import java.util.Arrays;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    ConstraintLayout cl_gameLayout;
    ConstraintLayout cl_towerLayout;
    TextView txt_towerName;

    ImageView tower_1;
    ImageView tower_2;
    ImageView tower_3;
    ImageView tower_4;
    ImageView tower_5;
    ImageView tower_6;
    ImageView tower_7;
    ImageView tower_8;
    ImageView tower_9;
    ImageView tower_10;
    ImageView tower_11;

    List<ImageView> towerList;

    Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        cl_gameLayout = findViewById(R.id.cl_gameLayout);
        cl_towerLayout = findViewById(R.id.cl_towerLayout);
        txt_towerName = findViewById(R.id.txt_towerName);

        tower_1 = findViewById(R.id.img_Tower1);
        tower_2 = findViewById(R.id.img_Tower2);
        tower_3 = findViewById(R.id.img_Tower3);
        tower_4 = findViewById(R.id.img_Tower4);
        tower_5 = findViewById(R.id.img_Tower5);
        tower_6 = findViewById(R.id.img_Tower6);
        tower_7 = findViewById(R.id.img_Tower7);
        tower_8 = findViewById(R.id.img_Tower8);
        tower_9 = findViewById(R.id.img_Tower9);
        tower_10 = findViewById(R.id.img_Tower10);
        tower_11 = findViewById(R.id.img_Tower11);

        towerList = Arrays.asList(tower_1, tower_2, tower_3, tower_4, tower_5, tower_6, tower_7,
                tower_8, tower_9, tower_10, tower_11);


        // display size
        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);



        cl_gameLayout.post(new Runnable() {
            @Override
            public void run() {

                // save state
                SaveState saveState = (SaveState) getIntent().getSerializableExtra("saveState");

                try {
                    game = new Game(GameActivity.this, cl_gameLayout.getWidth(), cl_gameLayout.getHeight(), saveState);
                } catch (Exception e) {
                    // redirect game errors to logcat
                    Log.e(getString(R.string.logcatKey), Log.getStackTraceString(e));
                }

                cl_gameLayout.addView(game);

            }
        });



    }


    /**
     * This method is for when one of the ImageView objects representing towers is clicked.
     * It sets the text of the txt_towerName to the tower name.
     *
     * @param view view
     */
    public void towerSelected(View view) {
        for (int i = 0; i < towerList.size(); i++) {
            if (towerList.get(i).isPressed()) {
                ImageView imageView = (ImageView)findViewById(towerList.get(i).getId());
                String imageName = String.valueOf(imageView.getTag());

                txt_towerName.setText(imageName);
            }
        }
    }

    public void gameOver(){

        // Go back to game selection
        Intent intent = new Intent().setClass(this, GameSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Log.i(getString(R.string.logcatKey),"Game Over. Returning to Game Select Menu.");
    }


}