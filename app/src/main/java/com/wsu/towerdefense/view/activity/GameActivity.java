package com.wsu.towerdefense.view.activity;

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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.wsu.towerdefense.audio.AdvancedSoundPlayer;
import com.wsu.towerdefense.Model.tower.Tower;
import com.wsu.towerdefense.Model.tower.TowerUpgradeData;
import com.wsu.towerdefense.Model.tower.Upgrade;
import com.wsu.towerdefense.Model.Game;
import com.wsu.towerdefense.Model.Game.Difficulty;
import com.wsu.towerdefense.Model.save.SaveState;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;
import com.wsu.towerdefense.Util;
import com.wsu.towerdefense.audio.Music;

import java.util.ArrayList;
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

    private AdvancedSoundPlayer audioButtonPress;

    private ConstraintLayout cl_gameLayout;
    private ConstraintLayout cl_towerInfoLayout;
    private ConstraintLayout cl_upgradeInfoLayout;
    private ScrollView sv_tower;
    private boolean isTowerMenuScrollable;
    private TextView txt_towerName;
    private TextView txt_towerPurchasePrice;
    private Button btn_sellTower;

    private ImageButton btn_upgrade_info;
    private ProgressBar[] progBar;
    private TextView[] txt_upgradeName;
    private Button[] btn_upgrade;
    private TextView[] txt_upgradeDescriptions;

    private ImageButton btn_pause;
    private ImageButton btn_play;
    private ImageButton btn_fast_fwd;

    private ImageView img_selectedTowerBase;
    private ImageView img_selectedTowerTurret;
    private TextView txt_selectedTowerKillCount;

    private LinearLayout imageContainer;

    private List<ImageView> towerList;
    private Tower.Type selectedTowerType = null;   // temporarily holds the TowerType of dragged Tower

    private Game game;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        onWindowFocusChanged(true);

        audioButtonPress = new AdvancedSoundPlayer(R.raw.ui_button_press);

        Music.getInstance(this).playGame();

        cl_gameLayout = findViewById(R.id.cl_gameLayout);
        cl_towerInfoLayout = findViewById(R.id.cl_upgradeLayout);
        cl_upgradeInfoLayout = findViewById(R.id.cl_upgradeInfoLayout);
        img_selectedTowerBase = findViewById(R.id.img_towerImageBase);
        img_selectedTowerTurret = findViewById(R.id.img_towerImageTurret);
        txt_selectedTowerKillCount = findViewById(R.id.txt_kill_count);
        sv_tower = findViewById(R.id.sv_tower);
        txt_towerName = findViewById(R.id.txt_towerName);
        txt_towerPurchasePrice = findViewById(R.id.txt_towerPurchasePrice);
        btn_sellTower = findViewById(R.id.btn_sell);
        btn_upgrade_info = findViewById(R.id.btn_upgrade_info);
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
        btn_pause = findViewById(R.id.btn_pause);
        btn_play = findViewById(R.id.btn_play);
        btn_fast_fwd = findViewById(R.id.btn_fastForward);
        txt_upgradeDescriptions = new TextView[]{
                findViewById(R.id.txt_upgrade_1_info),
                findViewById(R.id.txt_upgrade_2_info),
                findViewById(R.id.txt_upgrade_3_info)
        };
        btn_pause = findViewById(R.id.btn_pause);
        btn_play = findViewById(R.id.btn_play);
        btn_fast_fwd = findViewById(R.id.btn_fastForward);
        imageContainer = findViewById(R.id.tower_list);

        scrollViewInit();
        isTowerMenuScrollable = true;

        btn_fast_fwd.setVisibility(View.GONE);
        btn_fast_fwd.setEnabled(false);
        towerList = new ArrayList<>();

        addTowerViews();

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

            btn_fast_fwd.setVisibility(View.GONE);
            btn_fast_fwd.setEnabled(false);

            btn_upgrade_info.setOnClickListener(view -> {
                audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));
                this.setSelectionInfoMenuVisible(this.cl_upgradeInfoLayout.getVisibility() == View.GONE);
            });

            btn_pause.setOnClickListener(view -> {
                audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

                game.setPaused(true);
                startActivity(new Intent(GameActivity.this, PauseActivity.class),
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            });

            btn_play.setOnClickListener(view -> {
                audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

                if (!game.getWaves().isRunning() && game.getEnemies().size() == 0) {
                    btn_play.setVisibility(View.GONE);
                    btn_play.setEnabled(false);
                    btn_fast_fwd.setVisibility(View.VISIBLE);
                    btn_fast_fwd.setEnabled(true);
                    game.save();
                    game.getWaves().nextWave();
                    game.setWaveRunning(true);
                }
            });

            btn_fast_fwd.setOnClickListener(view -> {
                if (game.isFastMode()) {
                    //set game to 1x speed
                    game.setFastMode(false);
                    int color = getColor(R.color.ff_off);
                    btn_fast_fwd.getBackground().setColorFilter(color, Mode.SRC_ATOP);
                } else {
                    game.setFastMode(true);
                    //set game to 2x speed
                    int color = getColor(R.color.ff_on);
                    btn_fast_fwd.getBackground().setColorFilter(color, Mode.SRC_ATOP);
                }
            });

            List<Tower> towers = game.getTowers();
            game.setOnTouchListener((view, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        for (Tower tower : towers) {
                            float dx = event.getX() - tower.getLocation().x;
                            float dy = event.getY() - tower.getLocation().y;
                            double distance = Math.hypot(dx, dy);

                            // check if distance from click to tower is within radius
                            if (distance < Tower.BASE_SIZE / 2 * SELECT_TOLERANCE) {
                                audioButtonPress
                                    .play(view.getContext(), Settings.getSFXVolume(view.getContext()));

                                isTowerMenuScrollable = false;
                                enableImageViews(towerList, false);
                                setSelectionMenuVisible(true);

                                // setting sell button text
                                btn_sellTower.setText("Sell For\n" + tower.getStats().getSellPrice());

                                // Notify game of selected tower
                                game.selectTower(tower);

                                updateUpgradeUI();

                                return true;
                            }
                        }
                        isTowerMenuScrollable = true;
                        enableImageViews(towerList, true);
                        setSelectionInfoMenuVisible(false);
                        setSelectionMenuVisible(false);
                        game.selectTower(null);
                        return true;
                    }
                    return false;
                }
            );

            // Add Custom listener to game
            game.setGameListener(new Game.GameListener() {
                @Override
                public void onMoneyChanged() {
                    updateTowerSelection();
                    runOnUiThread(() -> updateUpgradeUI());
                }

                @Override
                public void onGameOver(boolean won) {
                    gameOver();
                    updateScoresAndClose(game, won);
                }

                @Override
                public void onWaveEnd() {
                    runOnUiThread(() -> updatePlayBtn());
                }
            });

            updateTowerSelection();
        });
    }

    private void addTowerViews() {
        final int margin = Util.dpToPixels(getResources(), 5);

        towerList = new ArrayList<>();

        for (Tower.Type type : Tower.Type.values()) {
            ImageView image = new ImageView(this);
            image.setImageResource(type.uiResID);
            image.setOnClickListener(this::towerSelected);
            image.setTag(type.name + "-" + type.cost);

            LayoutParams layout = new LayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT)
            );
            layout.setMarginStart(margin);
            layout.setMarginEnd(margin);
            image.setLayoutParams(layout);

            towerList.add(image);
            imageContainer.addView(image);
        }
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
                selectedTowerType = Tower.Type.values()[_i];
                return true;
            });
            image.setOnDragListener((view, event) -> {
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
        cl_gameLayout.setOnDragListener((view, event) -> {
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
                audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

                String[] tag = String.valueOf(view.getTag()).split("-");
                String imageName = tag[0];
                String towerCost = tag[1];
                txt_towerName.setText(imageName);
                txt_towerPurchasePrice.setText("$" + towerCost);

                break;
            }
        }
    }


    private void updatePlayBtn() {
        btn_fast_fwd.setVisibility(View.GONE);
        btn_fast_fwd.setEnabled(false);
        btn_play.setVisibility(View.VISIBLE);
        btn_play.setEnabled(true);
    }

    private void updateTowerSelection() {
        int money = game.getMoney();

        for (int i = 0; i < towerList.size(); i++) {
            ImageView image = towerList.get(i);

            // Enable towers (in menu) with cost equal to or lower
            // than cost of their respective Type
            if (money >= Tower.Type.values()[i].cost) {
                image.setColorFilter(null);
                final int _i = i;
                image.setOnLongClickListener(view -> {
                    ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());
                    String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                    ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
                    View.DragShadowBuilder dragshadow = new View.DragShadowBuilder(view);
                    view.startDragAndDrop(data, dragshadow, view, 0);

                    // Get the tower Type using the image
                    selectedTowerType = Tower.Type.values()[_i];
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
     * This method goes to the UpdateScoresActivity
     */
    public void updateScoresAndClose(Game game, boolean won){
        /*  TODO Modify so a different activity is created depending on won.
             Both Activities get a userName then returns to menu. Differences are
             background image and message*/

        Intent intent = new Intent(GameActivity.this, UpdateScoresActivity.class);
        intent.putExtra("score", game.getScore());
        intent.putExtra("won", won);
        intent.putExtra("difficulty", game.getDifficulty().toString().toUpperCase());
        startActivity(intent);
    }

    /**
     * This method goes to the GameSelectionActivity, then kills all tasks related to the current
     * activity, including the extra Game thread.
     */
    private void gameOver() {
        Intent intent = new Intent().setClass(this, GameSelectionActivity.class);
        startActivity(intent);
    }

    /**
     * Called when remove button is clicked
     */
    public void removeSelectedTower(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        game.removeSelectedTower();
        setSelectionMenuVisible(false);
        setSelectionInfoMenuVisible(false);
        enableImageViews(towerList, true);
        isTowerMenuScrollable = true;
    }

    /**
     * Shows/hides selected tower menu
     */
    private void setSelectionMenuVisible(boolean visible) {
        cl_towerInfoLayout.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Shows/hides selected tower info menu
     */
    private void setSelectionInfoMenuVisible(boolean visible) {
        cl_upgradeInfoLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
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
        this.sv_tower.setOnTouchListener((view, event) -> !GameActivity.this.isTowerMenuScrollable);
    }

    /**
     * @param imageViews the image views to enable or disable
     * @param enable     if true, enable imageviews, otherwise disable them
     */
    private void enableImageViews(List<ImageView> imageViews, boolean enable) {
        for (ImageView imageView : imageViews) {
            imageView.setEnabled(enable);
        }
    }

    // upgrade button actions
    public void btn_upgrade_Clicked(View view) {
        int pathNumber = Integer.parseInt(view.getTag().toString()) - 1;

        progBar[pathNumber].setProgress(progBar[pathNumber].getProgress() + 33);

        Tower tower = game.getSelectedTower();
        Upgrade upgrade = tower.getStats().upgrade(pathNumber);
        game.removeMoney(upgrade.cost);

        updateUpgradeUI();

        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));
    }

    /**
     * Sets the kill count label respective to the selected tower
     *
     * @param tower The tower that will provide the kill count value to the label
     */
    private void setTowerKillCountLabel(Tower tower){
        this.txt_selectedTowerKillCount.setText(String.valueOf(tower.getKillCount()));
    }

    private void updateUpgradeUI() {
        Tower tower = game.getSelectedTower();
        if (tower != null) {
            this.updateSelectedTowerImage(tower);
            this.setTowerKillCountLabel(tower);
            for (int path = 0; path < TowerUpgradeData.NUM_PATHS; path++) {
                TextView text = txt_upgradeName[path];
                Button button = btn_upgrade[path];
                TextView description = txt_upgradeDescriptions[path];

                // setting the upgrade names and costs
                if (!tower.getStats().isMaxUpgraded(path)) {
                    int upgradeCost = tower.getStats().getUpgrade(path, true).cost;

                    text.setText(tower.getStats().getUpgrade(path, true).displayName);
                    button.setText(String.format(
                        getString(R.string.money),
                        upgradeCost
                    ));

                    if (upgradeCost <= game.getMoney()) {
                        button.setVisibility(View.VISIBLE);
                        button.setAlpha(1f);
                        button.setEnabled(true);
                    } else {
                        button.setAlpha(0.5f);
                        button.setEnabled(false);
                    }

                    // set upgrade description
                    description.setText(tower.getStats().getUpgrade(path, true).description);

                } else {
                    text.setText(R.string.max);
                    description.setText(R.string.fully_upgraded);
                    button.setVisibility(View.INVISIBLE);
                    button.setEnabled(false);
                    button.setText("");
                }

                // setting the upgrade progress bars to 0, this should be changed to
                // show the current towers current upgrade progress
                progBar[path].setProgress(tower.getStats().getUpgradeProgress(path) * 33);
            }

            btn_sellTower.setText(String.format(
                getString(R.string.sell_for),
                tower.getStats().getSellPrice()
            ));
        }
    }

    /**
     * Sets the image in the cl_upgradeInfoLayout to the selected tower.
     *
     * @param tower represents the current selected tower
     */
    private void updateSelectedTowerImage(Tower tower){
        this.img_selectedTowerBase.setImageBitmap(tower.getBitmap());
        this.img_selectedTowerTurret.setImageBitmap(tower.getStats().getTurretImage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioButtonPress.release();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Util.hideNavigator(getWindow());
        }
    }
}