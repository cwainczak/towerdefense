package com.wsu.towerdefense.activity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.wsu.towerdefense.Game;
import com.wsu.towerdefense.Game.Difficulty;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.save.SaveState;
import com.wsu.towerdefense.tower.Tower;
import com.wsu.towerdefense.tower.TowerUpgradeData;
import java.util.Arrays;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    /**
     * Multiplies radius of valid tower selection
     */
    private static final double SELECT_TOLERANCE = 1.5;
    /**
     * Tint by which tower images are multiplied when there is not enough money
     */
    private static final PorterDuffColorFilter NO_MONEY_TINT = new PorterDuffColorFilter(
        Color.argb(255, 180, 0, 0),
        Mode.MULTIPLY
    );

    private ConstraintLayout cl_gameLayout;
    private ConstraintLayout cl_towerInfoLayout;
    private ScrollView sv_tower;
    private boolean isTowerMenuScrollable;
    private TextView txt_towerName;
    private Button btn_sellTower;

    private ProgressBar[] progBar;
    private TextView[] txt_upgradeName;
    private Button[] btn_upgrade;

    private List<ImageView> towerList;
    private List<Tower.Type> towerTypes;
    private Tower.Type selectedTowerType = null;   // temporarily holds the TowerType of dragged Tower

    private Game game;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        onWindowFocusChanged(true);

        cl_gameLayout = findViewById(R.id.cl_gameLayout);
        cl_towerInfoLayout = findViewById(R.id.cl_upgradeLayout);

        sv_tower = findViewById(R.id.sv_tower);
        scrollViewInit();
        isTowerMenuScrollable = true;

        txt_towerName = findViewById(R.id.txt_towerName);

        btn_sellTower = findViewById(R.id.btn_sell);

        progBar = new ProgressBar[]{
            findViewById(R.id.progBar_1),
            findViewById(R.id.progBar_2),
            findViewById(R.id.progBar_3)
        };

        txt_upgradeName = new TextView[]{
            findViewById(R.id.txt_upgradeName_1),
            findViewById(R.id.txt_upgradeName_2),
            findViewById(R.id.txt_upgradeName_3)
        };

        btn_upgrade = new Button[]{
            findViewById(R.id.btn_upgrade_1),
            findViewById(R.id.btn_upgrade_2),
            findViewById(R.id.btn_upgrade_3)
        };

        towerList = Arrays.asList(
            findViewById(R.id.img_Tower1),
            findViewById(R.id.img_Tower2)
        );

        towerTypes = Arrays.asList(
            Tower.Type.BASIC_LINEAR,
            Tower.Type.BASIC_HOMING
        );

        addDragListeners();

        // display size
        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        cl_gameLayout.post(() -> {
            // save state
            SaveState saveState = (SaveState) getIntent().getSerializableExtra("saveState");

            String map = getIntent().getStringExtra("map");
            Difficulty difficulty = (Difficulty) getIntent().getSerializableExtra("difficulty");

            try {
                game = new Game(
                    GameActivity.this,
                    cl_gameLayout.getWidth(),
                    cl_gameLayout.getHeight(),
                    saveState,
                    map,
                    difficulty
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
                startActivity(new Intent(GameActivity.this, PauseActivity.class),
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            });

            btn_play.setOnClickListener(view -> {
                if (!game.getWaves().isRunning() && game.getEnemies().size() == 0) {
                    game.save();
                    game.getWaves().nextWave();
                    game.getWaves().setRunning(true);
                }
            });

            List<Tower> towers = game.getTowers();
            game.setOnTouchListener((v, event) -> {
                    for (Tower tower : towers) {
                        float dx = event.getX() - tower.getLocation().x;
                        float dy = event.getY() - tower.getLocation().y;
                        double distance = Math.hypot(dx, dy);

                        // check if distance from click to tower is within radius
                        if (distance < Tower.BASE_SIZE / 2 * SELECT_TOLERANCE) {
                            isTowerMenuScrollable = false;
                            enableOrDisableImageViews(towerList, false);
                            setSelectionMenuVisible(true);

                            // setting sell button text
                            btn_sellTower.setText("Sell for: $" + tower.getCost() / 2);

                            // Notify game of selected tower
                            game.selectTower(tower);

                            updateUpgradeUI();

                            return true;
                        }
                    }
                    isTowerMenuScrollable = true;
                    enableOrDisableImageViews(towerList, true);
                    setSelectionMenuVisible(false);
                    game.selectTower(null);
                    return false;
                }
            );

            // Add Custom listener to game
            game.setGameListener(new Game.GameListener() {
                @Override
                public void onMoneyChanged() {
                    updateTowerSelection();
                }
            });

            updateTowerSelection();
        });
    }

    private void addDragListeners() {

        // add drag listeners to towers
        for (int i = 0; i < towerList.size(); i++) {
            ImageView image = towerList.get(i);

            final int _i = i;
            image.setOnLongClickListener(v -> {
                ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
                String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData data = new ClipData(v.getTag().toString(), mimeTypes, item);
                View.DragShadowBuilder dragshadow = new View.DragShadowBuilder(v);
                v.startDragAndDrop(data, dragshadow, v, 0);

                // Get the tower Type using the image
                selectedTowerType = towerTypes.get(_i);
                return true;
            });
            image.setOnDragListener((v, event) -> {
                // allow image to be dragged
                if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                    game.setDragType(selectedTowerType);
                    return true;
                }
                // remove range circle when dragging over side bar
                else if (event.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
                    game.drag(null);
                    game.selectTower(null);
                    setSelectionMenuVisible(false);
                    return true;
                } else if (event.getAction() == DragEvent.ACTION_DROP) {
                    game.drag(null);
                    game.setDragType(null);
                    game.selectTower(null);
                    setSelectionMenuVisible(false);
                    return true;
                }
                return false;
            });

        }

        // add drop listener to game
        cl_gameLayout.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                return true;
            }
            // show range circle when dragging over map
            else if (event.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
                PointF dragLocation = new PointF((event.getX()), event.getY());
                boolean onScreen = dragLocation.x < cl_gameLayout.getWidth() &&
                    dragLocation.y < cl_gameLayout.getHeight();
                if (onScreen) {
                    game.drag(dragLocation);
                    game.selectTower(null);
                    setSelectionMenuVisible(false);
                } else {
                    game.drag(null);
                }
            }
            // drop tower onto game
            else if (event.getAction() == DragEvent.ACTION_DROP) {
                game.drag(null);
                return game.placeTower(new PointF(event.getX(), event.getY()), selectedTowerType);
            }
            return false;
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

    private void updateTowerSelection() {
        int money = game.getMoney();

        for (int i = 0; i < towerList.size(); i++) {
            ImageView image = towerList.get(i);

            // Enable towers (in menu) with cost equal to or lower
            // than cost of their respective Type
            if (money >= towerTypes.get(i).cost) {
                image.setColorFilter(null);
                final int _i = i;
                image.setOnLongClickListener(v -> {
                    ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
                    String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                    ClipData data = new ClipData(v.getTag().toString(), mimeTypes, item);
                    View.DragShadowBuilder dragshadow = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(data, dragshadow, v, 0);

                    // Get the tower Type using the image
                    selectedTowerType = towerTypes.get(_i);
                    return true;
                });
            }
            // Disable towers (in menu) with cost greater than money
            else {
                image.setColorFilter(NO_MONEY_TINT);
                image.setOnLongClickListener(null);
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
        enableOrDisableImageViews(towerList, true);
        isTowerMenuScrollable = true;
    }

    /**
     * Called when pause button is clicked
     */
    public void btnPauseOnClick(View view) {
        game.setPaused(true);
        startActivity(new Intent(GameActivity.this, PauseActivity.class));
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

    /**
     * Initializes the Tower ScrollView, allowing the scroll to be disabled Enabled when
     * isTowerMenuScrollable is True Disabled when isTowerMenuScrollable is False
     */
    @SuppressLint("ClickableViewAccessibility")
    private void scrollViewInit() {
        this.sv_tower.setOnTouchListener((v, event) -> !GameActivity.this.isTowerMenuScrollable);
    }

    /**
     * @param imageViews the image views to enable or disable
     * @param enable     if true, enable imageviews, otherwise disable them
     */
    private void enableOrDisableImageViews(List<ImageView> imageViews, boolean enable) {
        for (ImageView imageView : imageViews) {
            imageView.setEnabled(enable);
        }
    }

    // upgrade button actions
    public void btn_upgrade_Clicked(View view) {
        int pathNumber = Integer.parseInt(view.getTag().toString()) - 1;

        progBar[pathNumber].setProgress(progBar[pathNumber].getProgress() + 33);
        game.getSelectedTower().getStats().upgrade(pathNumber);

        updateUpgradeUI();
    }

    private void updateUpgradeUI() {
        Tower tower = game.getSelectedTower();

        for (int path = 0; path < TowerUpgradeData.NUM_PATHS; path++) {
            TextView text = txt_upgradeName[path];
            Button button = btn_upgrade[path];

            // setting the upgrade names and costs
            if (!tower.getStats().isMaxUpgraded(path)) {
                text.setText(tower.getStats().getUpgrade(path, true).displayName);
                button.setVisibility(View.VISIBLE);
                button.setEnabled(true);
                button.setText("$" + tower.getStats().getUpgrade(path, true).cost);
            } else {
                text.setText(R.string.max);
                button.setVisibility(View.INVISIBLE);
                button.setEnabled(false);
                button.setText("");
            }

            // setting the upgrade progress bars to 0, this should be changed to
            // show the current towers current upgrade progress
            progBar[path].setProgress(tower.getStats().getUpgradeProgress(path) * 33);
        }
    }
}