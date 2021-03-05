package com.wsu.towerdefense.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.PointF;
import android.content.Intent;
import android.util.Log;
import android.view.DragEvent;
import android.view.View.OnDragListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import android.view.View;
import android.widget.Button;

import com.wsu.towerdefense.Application;
import com.wsu.towerdefense.Game;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Tower;
import com.wsu.towerdefense.save.SaveState;

import java.util.Arrays;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    ConstraintLayout cl_gameLayout;
    ConstraintLayout cl_towerLayout;
    ConstraintLayout cl_towerInfoLayout;
    TextView txt_towerName;
    TextView txt_towerInfo;

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

    private int towerSelectedIndex = -1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        onWindowFocusChanged(true);

        cl_gameLayout = findViewById(R.id.cl_gameLayout);
        cl_towerLayout = findViewById(R.id.cl_towerLayout);
        cl_towerInfoLayout = findViewById(R.id.cl_towerInfoLayout);

        txt_towerName = findViewById(R.id.txt_towerName);
        txt_towerInfo = findViewById(R.id.txt_towerInfo);

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

        // add drag listeners to towers
        OnDragListener towerListener = (v, event) -> {
            // allow image to be dragged
            return event.getAction() == DragEvent.ACTION_DRAG_STARTED;
        };
        for (int i = 0; i < towerList.size(); i++) {
            ImageView image = towerList.get(i);

            image.setOnLongClickListener(v -> {
                    ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
                    String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                    ClipData data = new ClipData(v.getTag().toString(), mimeTypes, item);
                    View.DragShadowBuilder dragshadow = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(data, dragshadow, v, 0);
                    return true;
                }
            );
            image.setOnDragListener(towerListener);
        }
        // add drop listener to game
        cl_gameLayout.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                return true;
            } else if (event.getAction() == DragEvent.ACTION_DROP) {
                // drop tower onto game
                return game.placeTower(event.getX(), event.getY());
            }
            return false;
        });

        // display size
        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        cl_gameLayout.post(() -> {
            // save state
            SaveState saveState = (SaveState) getIntent().getSerializableExtra("saveState");

            try {
                game = new Game(
                    GameActivity.this,
                    cl_gameLayout.getWidth(),
                    cl_gameLayout.getHeight(),
                    saveState
                );
            } catch (Exception e) {
                // redirect game errors to logcat
                Log.e(getString(R.string.logcatKey), Log.getStackTraceString(e));
            }

            cl_gameLayout.addView(game);

            ImageButton btn_pause = findViewById(R.id.btn_pause);

            btn_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    game.setPaused(true);
                    startActivity(new Intent(GameActivity.this, PauseActivity.class));
                }
            });

            setSelectionMenuVisible(false);

            List<Tower> towers = game.getTowers();
            game.setOnTouchListener((v, event) -> {

                for (int i = 0; i < towers.size(); i++) {
                    Tower tower = towers.get(i);

                    PointF delta = new PointF(
                        event.getX() - tower.getLocation().x,
                        event.getY() - tower.getLocation().y
                    );
                    double distance = Math.hypot(delta.x, delta.y);

                    // check if distance from click to tower is within radius
                    if (distance < Game.towerRadius) {
                        towerSelectedIndex = i;
                        setSelectionMenuVisible(true);

                        // temporary position text
                        txt_towerInfo.setText(
                            "x: " + tower.getLocation().x +
                                ", y: " + tower.getLocation().y
                        );
                        return true;
                    }
                }
                towerSelectedIndex = -1;
                setSelectionMenuVisible(false);
                return false;
            });

        });

    }


    /**
     * This method is for when one of the ImageView objects representing towers is clicked. It sets
     * the text of the txt_towerName to the tower name.
     *
     * @param view view
     */
    public void towerSelected(View view) {
        for (int i = 0; i < towerList.size(); i++) {
            if (towerList.get(i).isPressed()) {
                ImageView imageView = findViewById(towerList.get(i).getId());
                String imageName = String.valueOf(imageView.getTag());

                txt_towerName.setText(imageName);
            }
        }
    }

    public void gameOver() {
        // Go back to game selection
        Intent intent = new Intent().setClass(this, GameSelectionActivity.class);
        startActivity(intent);
        finishAffinity();
        Log.i(getString(R.string.logcatKey), "Game Over. Returning to Game Select Menu.");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ActivityUtil.hideNavigator(getWindow());
        }
    }

    /**
     * Called when remove button is clicked
     */
    public void removeSelectedTower(View view) {
        game.removeTower(towerSelectedIndex);
        setSelectionMenuVisible(false);
    }

    /**
     * Shows/hides selected tower menu
     */
    private void setSelectionMenuVisible(boolean visible) {
        cl_towerInfoLayout.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (game != null) {
            game.setPaused(false);
        }
    }
}