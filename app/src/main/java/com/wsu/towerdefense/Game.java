package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import com.wsu.towerdefense.activity.GameActivity;
import com.wsu.towerdefense.map.Map;
import com.wsu.towerdefense.map.MapReader;
import com.wsu.towerdefense.save.SaveState;
import com.wsu.towerdefense.save.Serializer;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Game extends AbstractGame {

    private static final int START_LIVES = 5;
    private static final int START_MONEY = 300;
    private static final int VALID_RANGE_COLOR = Color.argb(255, 80, 80, 80);
    private static final int INVALID_RANGE_COLOR = Color.argb(255, 180, 0, 0);
    private static final int RANGE_OPACITY = 90;

    private final List<Tower> towers;
    private final List<Enemy> enemies;

    private final Waves waves;

    private final Map map;

    public static final float towerRadius = 56; //radius of tower object using tower.png

    private int lives;

    /**
     * The money available for the player to spend
     */
    private int money;
    /**
     * A custom listener used to send data to the GameActivity whenever certain actions occur
     */
    private GameListener listener = null;

    /**
     * The next tower to be added; prevents {@link java.util.ConcurrentModificationException} when
     * iterating
     */
    private Tower addBuffer = null;

    /**
     * The currently selected tower
     */
    private Tower selectedTower = null;
    /**
     * Whether or not to remove the currently selected tower
     */
    private boolean removeTower = false;

    public PointF dragLocation = null;

    public Game(Context context, int gameWidth, int gameHeight, SaveState saveState,
        String mapName){
        super(context, gameWidth, gameHeight);

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
//        waves = hasSave ? saveState.waves : new Waves();
        waves = new Waves(context);
        towers = hasSave ? saveState.towers : new ArrayList<>();
        lives = hasSave ? saveState.lives : START_LIVES;
        money = hasSave ? saveState.money : START_MONEY;

        enemies = new ArrayList<>();

        Log.i(context.getString(R.string.logcatKey),
            "Started game with map '" + map.getName() + "'"
        );
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

    @Override
    protected void update(double delta) {
        spawnEnemy(delta);
        // Update the Enemies, remove any dead Enemies
        for (Iterator<Enemy> enemyIt = enemies.iterator(); enemyIt.hasNext(); ) {
            Enemy e = enemyIt.next();

            if (e.isAlive()) {
                e.update(this, delta);

                if (e.isAtPathEnd()) {
                    lives--;
                    enemyIt.remove();
                    if (lives <= 0) {
                        gameOver();
                        return;
                    }
                }

            } else {
                // Add enemy's value to game balance
                addMoney(e.getPrice());

                // Remove dead Enemies
                enemyIt.remove();
            }
        }

        checkBuffers();

        // Update the Towers
        for (Tower t : towers) {
            t.update(this, delta);
        }
    }

    @Override
    protected void render(double lerp, Canvas canvas, Paint paint) {
        map.render(canvas, paint);

        for (Tower t : towers) {
            // Draw all tower ranges if in debug mode
            if (Application.DEBUG) {
                t.drawLine(canvas, paint);
            }
            if (selectedTower == t) {
                drawRange(canvas, paint, t.getLocation(), t.getRange(), true);
            }

            t.render(lerp, canvas, paint);
        }

        for (Enemy e : enemies) {
            e.render(lerp, canvas, paint);
        }

        drawHUD(canvas, paint);

        if (dragLocation != null) {
            // TODO: change hardcoded radius based on dragged tower type
            drawRange(canvas, paint, dragLocation, 384, isValidPlacement(dragLocation));
        }
    }

    private void checkBuffers() {
        // add tower from buffer
        if (addBuffer != null) {
            towers.add(addBuffer);
            addBuffer = null;
        }

        // remove tower from list based on buffer
        if (removeTower) {
            towers.remove(selectedTower);
            selectedTower = null;
            removeTower = false;
        }
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
        int yOffset = 80;

        paint.reset();
        paint.setShadowLayer(0.1f, 5, 5, Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(75);

        paint.setColor(Color.YELLOW);
        canvas.drawText("$" + money, posX, posY, paint);

        paint.setColor(Color.WHITE);
        canvas.drawText("Lives: " + lives, posX, posY + yOffset, paint);
    }

    /**
     * Place a tower at given coordinates if placement is valid
     *
     * @param x x
     * @param y y
     * @return whether position is valid
     */
    public boolean placeTower(float x, float y) {
        // Todo - Cost will need to be passed from GameActivity once there are different towers
        int cost = 100;

        if (isValidPlacement(new PointF(x, y)) && cost <= money) {
            addBuffer = new Tower(new PointF(x, y), 384, 750f, 10, cost);

            // purchase tower
            removeMoney(addBuffer.cost);
            return true;
        }

        return false;
    }

    public void removeSelectedTower() {
        removeTower = true;

        // refund tower cost
        addMoney(selectedTower.cost / 2);
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
        for (Tower tower : getTowers()) {
            double distance = distanceToPoint(location, tower.getLocation());
            if (distance < towerRadius * 2) {
                return false;
            }
        }

        // check against path
        for (RectF rect : map.getBounds()) {
            if (rect.left - towerRadius <= location.x &&
                location.x - towerRadius <= rect.right &&
                rect.top - towerRadius <= location.y &&
                location.y - towerRadius <= rect.bottom) {
                return false;
            }
        }
        return true;
    }

    private double distanceToPoint(PointF location, PointF point) {
        float dx = location.x - point.x;
        float dy = location.y - point.y;
        return Math.hypot(dx, dy);
    }

    /**
     * Ends this game and returns to the the menu
     */
    private void gameOver() {
        // stop game loop
        running = false;

        // delete save file
        Serializer.delete(getContext(), Serializer.SAVEFILE);

        // return to menu
        ((GameActivity) getContext()).gameOver();
    }

    public void spawnEnemy(double delta){
        if(waves.isRunning()) {
            waves.updateTimeSinceSpawn(delta);

            if (waves.delayPassed()){
                enemies.add(new Enemy(waves.next(), map.getPath()));
            }
        }
    }

    public void setSelectedTower(Tower tower) {
        selectedTower = tower;
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
        paint.setColor(valid ? VALID_RANGE_COLOR : INVALID_RANGE_COLOR);
        paint.setAlpha(RANGE_OPACITY);
        canvas.drawCircle(location.x, location.y, radius, paint);
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
    private void removeMoney(int amount) {
        money -= amount;
        listener.onMoneyChanged();
    }

    public int getMoney() {
        return money;
    }

    public Map getMap() {
        return map;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Tower> getTowers() {
        return towers;
    }

    public int getLives() {
        return lives;
    }

    public Waves getWaves() {
        return waves;
    }

    /**
     * A custom listener for Game objects
     */
    public interface GameListener {
        /**
         * This method is called whenever the game's money increases or decreases
         */
        void onMoneyChanged();
    }

    public void setGameListener(GameListener listener) {
        this.listener = listener;
    }


    //TEMP VALUES FOR TESTING
    List<List<Integer>> a1 = Arrays.asList(
            Arrays.asList(3, 2),
            Arrays.asList(5, 4, 3),
            Arrays.asList(2, 1, 2)
    );

    List<List<Double>> d1 = Arrays.asList(
            Arrays.asList(0.5, 0.6),
            Arrays.asList(0.1, 0.2, 0.3),
            Arrays.asList(0.03, 0.07, 0.02)
    );

    List<List<Enemy.Type>> t1 = Arrays.asList(
            Arrays.asList(Enemy.Type.S1, Enemy.Type.S2),
            Arrays.asList(Enemy.Type.S1, Enemy.Type.S2, Enemy.Type.S3),
            Arrays.asList(Enemy.Type.S1, Enemy.Type.S2, Enemy.Type.S3)
    );
}