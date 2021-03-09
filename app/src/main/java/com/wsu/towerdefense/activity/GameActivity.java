package com.wsu.towerdefense.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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

    List<ImageView> towerList;

    Game game;

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

        towerList = Arrays.asList(
            findViewById(R.id.img_Tower1),
            findViewById(R.id.img_Tower2),
            findViewById(R.id.img_Tower3),
            findViewById(R.id.img_Tower4),
            findViewById(R.id.img_Tower5),
            findViewById(R.id.img_Tower6),
            findViewById(R.id.img_Tower7),
            findViewById(R.id.img_Tower8),
            findViewById(R.id.img_Tower9),
            findViewById(R.id.img_Tower10),
            findViewById(R.id.img_Tower11)
        );

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
            ImageButton btn_play = findViewById(R.id.btn_play);

            btn_pause.setOnClickListener(view -> {
                game.setPaused(true);
                startActivity(new Intent(GameActivity.this, PauseActivity.class));
            });

            btn_play.setOnClickListener(view -> {
                if(game.getEnemies().isEmpty()){
                    game.spawnEnemies();
                }
            });

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
                        setSelectionMenuVisible(true);

                        // temporary position text
                        txt_towerInfo.setText(
                            "x: " + tower.getLocation().x +
                            "\ny: " + tower.getLocation().y +
                            "\n\nSell for: " + tower.cost/2
                        );

                        // Notify game of selected tower
                        game.setSelectedTower(tower);

                        return true;
                    }
                }
                setSelectionMenuVisible(false);
                game.setSelectedTower(null);
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

    /**
     * This method goes to the GameSelectionActivity, then kills all tasks related to the current
     * activity, including the extra Game thread.
     */
    public void gameOver() {
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
        game.removeSelectedTower();
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