package com.wsu.towerdefense.Model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import com.wsu.towerdefense.AbstractGame;
import com.wsu.towerdefense.MapReader;
import com.wsu.towerdefense.Model.Enemy.Type;
import com.wsu.towerdefense.audio.AdvancedSoundPlayer;
import com.wsu.towerdefense.audio.BasicSoundPlayer;
import com.wsu.towerdefense.audio.SoundSource;
import com.wsu.towerdefense.map.Map;
import com.wsu.towerdefense.Model.tower.Tower;
import com.wsu.towerdefense.MapEvent;
import com.wsu.towerdefense.Model.save.SaveState;
import com.wsu.towerdefense.Model.save.Serializer;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Game extends AbstractGame implements SoundSource {

    private static final int START_LIVES = 25;
    private static final int START_MONEY = 400;
    private static final int START_SCORE = 0;

    private static final int RANGE_OPACITY = 90;
    public final int validRangeColor;
    public final int invalidRangeColor;

    private final BasicSoundPlayer audioPlaceTower;
    private final AdvancedSoundPlayer audioLoseLife;

    private final List<Tower> towers;
    private final List<Enemy> enemies;
    private final Waves waves;
    private final Difficulty difficulty;
    private final Map map;

    private int lives;
    private int money;
    private int score;

    private boolean waveRunning = false;
    private boolean isFastMode = false;

    /**
     * A custom listener used to send data to the GameActivity whenever certain actions occur
     */
    private GameListener listener = null;
    private Tower selectedTower = null;
    private PointF dragLocation = null;
    private Tower.Type dragType = null;
    private final List<MapEvent> mapEvents;

    public Game(Context context, int gameWidth, int gameHeight, SaveState saveState,
        String mapName, Difficulty difficulty) {
        super(context, gameWidth, gameHeight);

        audioPlaceTower = new BasicSoundPlayer(context, R.raw.game_tower_place, false);
        audioLoseLife = new AdvancedSoundPlayer(R.raw.ui_button_deny);

        mapEvents = new ArrayList<>();

        validRangeColor = getResources().getColor(R.color.valid_range, null);
        invalidRangeColor = getResources().getColor(R.color.invalid_range, null);

        // game state

        boolean hasSave = saveState != null;
        if (hasSave) {
            Log.i(context.getString(R.string.logcatKey),
                "Loading save file '" + saveState.saveFile + "'"
            );
        }

        map = new Map(
            MapReader.get(hasSave ? saveState.mapName : mapName),
            getGameWidth(),
            getGameHeight()
        );
        this.difficulty = hasSave ? saveState.difficulty : difficulty;
        waves = hasSave ? saveState.waves : new Waves(context, difficulty);
        towers = hasSave ? saveState.towers : new ArrayList<>();
        lives = hasSave ? saveState.lives : START_LIVES;
        money = hasSave ? saveState.money : START_MONEY;
        score = hasSave ? saveState.score : START_SCORE;

        enemies = new ArrayList<>();

        Log.i(context.getString(R.string.logcatKey),
            "Started game with map '" + map.getName() + "'" +
                " and difficulty '" + this.difficulty.toString() + "'"
        );
    }

    /**
     * Ends this game and returns to the the menu
     */
    public void gameOver(boolean won) {
        running = false;

        this.release();

        Serializer.delete(getContext(), Serializer.SAVEFILE);

        // return to menu
        listener.onGameOver(won);
    }

    /**
     * Saves the current game state to the default save file
     */
    public void save() {
        try {
            Serializer.save(getContext(), Serializer.SAVEFILE, this);
        } catch (IOException e) {
            Log.e(getContext().getString(R.string.logcatKey), "Error while saving", e);
        }
    }

    // GAME STATE

    @Override
    protected void update(double delta) {

        // Update the Enemies, remove any dead Enemies
        for (Iterator<Enemy> enemyIt = enemies.iterator(); enemyIt.hasNext(); ) {
            Enemy e = enemyIt.next();

            if (e.isAlive()) {
                e.update(this, delta);

                if (e.isAtPathEnd()) {
                    lives -= e.getType().getDamage();
                    enemyIt.remove();
                    if (lives <= 0) {
                        lives = 0;
                        gameOver(false);
                    }
                    audioLoseLife.play(getContext(), Settings.getSFXVolume(getContext()));
                }
            } else {
                // Add enemy's value to game balance and score
                addMoney((int) (e.getPrice() * difficulty.priceModifier));
                addScore((int) (e.getPrice() * difficulty.priceModifier));

                // Remove dead Enemies
                enemyIt.remove();
            }
        }

        // Update Waves
        waves.update(this, delta);
        if (!waves.isRunning() && enemies.isEmpty() && waveRunning) {
            waveRunning = false;
            if (waves.isGameEnded()) {
                listener.onGameOver(true);
            } else {
                listener.onWaveEnd();
            }
        }

        handleEvents();

        // Update the Towers
        for (Tower t : towers) {
            t.update(this, delta);
        }
    }

    // RENDERING

    @Override
    protected void render(double lerp, Canvas canvas, Paint paint) {
        paint.reset();
        map.render(canvas, paint);

        for (Tower t : towers) {
            // Draw all tower ranges if in debug mode
//            if (Application.DEBUG) {
//                t.drawLine(canvas, paint);
//            }
            if (selectedTower == t) {
                drawRange(canvas, paint, t.getLocation(),
                    t.getType() == Tower.Type.SNIPER ? Tower.BASE_SIZE : t.getStats().getRange(),
                    true);
            }

            t.render(lerp, canvas, paint);
        }

        for (Enemy e : enemies) {
            e.render(lerp, canvas, paint);
        }

        if (dragLocation != null) {
            drawRange(canvas, paint, dragLocation,
                dragType == Tower.Type.SNIPER ? Tower.BASE_SIZE : dragType.range,
                isValidPlacement(dragLocation));
        }

        drawHUD(canvas, paint);
    }

    /**
     * Calculates the distance between a new tower and every existing object to determine if the
     * placement of the new tower is valid. Assumes the new tower exists at time of valid check.
     *
     * @param location Location of new tower to be placed
     * @return True if valid placement, false if not
     */
    public boolean isValidPlacement(PointF location) {
        // check against towers
        for (Tower tower : towers) {
            if (tower.collides(location, Tower.BASE_SIZE, Tower.BASE_SIZE)) {
                return false;
            }
        }

        // check against path
        for (RectF rect : map.getBounds()) {
            if (rect.left - Tower.BASE_SIZE / 2 <= location.x &&
                location.x - Tower.BASE_SIZE / 2 <= rect.right &&
                rect.top - Tower.BASE_SIZE / 2 <= location.y &&
                location.y - Tower.BASE_SIZE / 2 <= rect.bottom) {
                return false;
            }
        }
        return true;
    }

    /**
     * Draws the money count and life count to the top left corner of the canvas
     *
     * @param canvas Canvas to draw the life count on
     * @param paint  Paint to draw with
     */
    private void drawHUD(Canvas canvas, Paint paint) {
        int posX = 10;
        int posY = 75;
        int yOffsetLives = 80;
        int yOffsetWaves = 160;
        int yOffsetScore = 1325;

        paint.reset();
        paint.setShadowLayer(0.1f, 5, 5, Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(75);

        paint.setColor(Color.YELLOW);
        canvas.drawText("$" + money, posX, posY, paint);

        paint.setColor(Color.WHITE);
        canvas.drawText("Lives: " + lives, posX, posY + yOffsetLives, paint);

        if (waves.getCurWave() == 0) {
            canvas.drawText("Wave: 1", posX, posY + yOffsetWaves, paint);
        } else {
            canvas.drawText("Wave: " + waves.getCurWave(), posX, posY + yOffsetWaves, paint);
        }

        paint.setColor(Color.WHITE);
        canvas.drawText("Score: " + score, posX, posY + yOffsetScore, paint);
    }

    /**
     * A helper method that draws a circular outline representing the range of this Tower.
     *
     * @param canvas The Canvas to draw the range on.
     * @param paint  The Paint used to draw the range.
     */
    public void drawRange(Canvas canvas, Paint paint,
        PointF location,
        float radius,
        boolean valid
    ) {
        paint.reset();
        paint.setColor(valid ? validRangeColor : invalidRangeColor);
        paint.setAlpha(RANGE_OPACITY);
        canvas.drawCircle(location.x, location.y, radius, paint);
    }

    public int getScore() {
        return score;
    }

    // UI

    /**
     * Sends game events to UI
     */
    public interface GameListener {

        void onMoneyChanged();

        void onWaveEnd();

        void onGameOver(boolean won);
    }

    public void setGameListener(GameListener listener) {
        this.listener = listener;
    }

    // MAP EVENTS

    private void handleEvents() {
        while (!mapEvents.isEmpty()) {
            MapEvent e = mapEvents.get(0);

            if (e instanceof MapEvent.PlaceTower) {
                towers.add(((MapEvent.PlaceTower) e).tower);
                audioPlaceTower.play(getContext(), Settings.getSFXVolume(getContext()));
            } else if (e instanceof MapEvent.RemoveTower) {
                selectedTower.release();
                towers.remove(selectedTower);
            } else if (e instanceof MapEvent.SpawnEnemy) {
                enemies.add(((MapEvent.SpawnEnemy) e).enemy);
            }

            mapEvents.remove(e);
        }
    }

    // PlaceTower event
    public boolean placeTower(PointF location, Tower.Type type) {
        if (isValidPlacement(new PointF(location.x, location.y)) && type.cost <= money) {
            Tower tower = new Tower(getContext(), new PointF(location.x, location.y), type);
            mapEvents.add(new MapEvent.PlaceTower(tower));

            // purchase tower
            removeMoney(type.cost);
            return true;
        }

        return false;
    }

    // RemoveTower event
    public void removeSelectedTower() {
        mapEvents.add(new MapEvent.RemoveTower());

        // refund tower cost
        addMoney(selectedTower.getStats().getSellPrice());
    }

    // SpawnEnemy event
    public void spawnEnemy(Type type) {
        mapEvents.add(new MapEvent.SpawnEnemy(new Enemy(getContext(), type, map.getPath())));
    }

    // DIFFICULTY ENUM
    public enum Difficulty {
        EASY(10, 1),
        MEDIUM(10, 0.9f),
        HARD(10, 0.8f);

        public final int waves;
        public final float priceModifier;


        Difficulty(int waves, float priceModifier) {
            this.waves = waves;
            this.priceModifier = priceModifier;
        }

        @Override
        public String toString() {
            switch (this) {
                case EASY:
                    return "Easy";
                case MEDIUM:
                    return "Medium";
                case HARD:
                    return "Hard";
                default:
                    return "";
            }
        }
    }

    // GETTERS/SETTERS

    /**
     * Adds a specified amount to the game's score (same as the amount of money gained)
     *
     * @param amount The amount of score to add
     */
    private void addScore(int amount) {
        score += amount;
    }

    /**
     * Adds a specified amount to the game's money and triggers the game's listener
     *
     * @param amount The amount of money to add
     */
    private void addMoney(int amount) {
        money += amount;
        listener.onMoneyChanged();
    }

    /**
     * Removes a specified amount from the game's money and triggers the game's listener
     *
     * @param amount The amount of money to remove
     */
    public void removeMoney(int amount) {
        money -= amount;
        listener.onMoneyChanged();
    }

    public List<Tower> getTowers() {
        return Collections.unmodifiableList(towers);
    }

    public List<Enemy> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    public Map getMap() {
        return map;
    }

    public int getLives() {
        return lives;
    }

    public Waves getWaves() {
        return waves;
    }

    public int getMoney() {
        return money;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void selectTower(Tower tower) {
        selectedTower = tower;
    }

    public void drag(PointF location) {
        dragLocation = location;
    }

    public Tower getSelectedTower() {
        return selectedTower;
    }

    public void setDragType(Tower.Type dragType) {
        this.dragType = dragType;
    }

    @Override
    public void release() {
        this.audioPlaceTower.release();
        this.audioLoseLife.release();
    }

    public void setWaveRunning(boolean waveRunning) {
        this.waveRunning = waveRunning;
        waves.setRunning(waveRunning);
    }

    public void setFastMode(boolean isFastMode) {
        this.isFastMode = isFastMode;
        this.setDoubleSpeed(isFastMode);
    }

    public boolean isFastMode() {
        return isFastMode;
    }
}