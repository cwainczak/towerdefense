package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import com.wsu.towerdefense.Enemy.Type;
import com.wsu.towerdefense.activity.GameActivity;
import com.wsu.towerdefense.map.Map;
import com.wsu.towerdefense.map.MapReader;
import com.wsu.towerdefense.save.SaveState;
import com.wsu.towerdefense.save.Serializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Game extends AbstractGame {

    private static final int START_LIVES = 5;
    private static final int START_MONEY = 300;
    private static final int RANGE_OPACITY = 90;
    public static final float towerRadius = 56; //radius of tower object using tower.png

    public final int validRangeColor;
    public final int invalidRangeColor;

    private final List<Tower> towers;
    private final List<Enemy> enemies;

    private final Map map;

    private int lives;
    private int money;

    /**
     * A custom listener used to send data to the GameActivity whenever certain actions occur
     */
    private GameListener listener = null;
    private Tower selectedTower = null;
    private PointF dragLocation = null;
    private final List<MapEvent> mapEvents;


    public Game(Context context, int gameWidth, int gameHeight, SaveState saveState,
        String mapName) {
        super(context, gameWidth, gameHeight);

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
        towers = hasSave ? saveState.towers : new ArrayList<>();
        lives = hasSave ? saveState.lives : START_LIVES;
        money = hasSave ? saveState.money : START_MONEY;

        enemies = new ArrayList<>();

        Log.i(context.getString(R.string.logcatKey),
            "Started game with map '" + map.getName() + "'"
        );
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

    /**
     * Saves the current game state to the default save file
     */
    private void save() {
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

        handleEvents();

        // Update the Towers
        for (Tower t : towers) {
            t.update(this, delta);
        }
    }

    // RENDERING

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
            // TODO: move into util
            float dx = location.x - tower.getLocation().x;
            float dy = location.y - tower.getLocation().y;
            double distance = Math.hypot(dx, dy);

            if (distance < towerRadius * 2) {
                return false;
            }
        }

        // check against path
        for (RectF rect : map.getBounds()) {
            if (rect.left <= location.x && location.x <= rect.right &&
                rect.top <= location.y && location.y <= rect.bottom) {
                return false;
            }
        }
        return true;
    }

    public void spawnEnemies() {
        if (enemies.isEmpty()) {
            save();

            spawnEnemy(Enemy.Type.S1);
            spawnEnemy(Enemy.Type.S2);
            spawnEnemy(Enemy.Type.S3);
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
        paint.setColor(valid ? validRangeColor : invalidRangeColor);
        paint.setAlpha(RANGE_OPACITY);
        canvas.drawCircle(location.x, location.y, radius, paint);
    }

    // UI

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

    // MAP EVENTS

    private void handleEvents() {
        while (!mapEvents.isEmpty()) {
            MapEvent e = mapEvents.get(0);

            if (e instanceof MapEvent.PlaceTower) {
                towers.add(((MapEvent.PlaceTower) e).tower);
            } else if (e instanceof MapEvent.RemoveTower) {
                towers.remove(selectedTower);
            } else if (e instanceof MapEvent.SpawnEnemy) {
                enemies.add(((MapEvent.SpawnEnemy) e).enemy);
            }

            mapEvents.remove(e);
        }
    }

    // PlaceTower event
    public boolean placeTower(PointF location) {
        // Todo - Cost will need to be passed from GameActivity once there are different towers
        int cost = 100;

        if (isValidPlacement(new PointF(location.x, location.y)) && cost <= money) {
            Tower tower = new Tower(new PointF(location.x, location.y), 384, 750f, 10, cost);
            mapEvents.add(new MapEvent.PlaceTower(tower));

            // purchase tower
            removeMoney(tower.cost);
            return true;
        }

        return false;
    }

    // RemoveTower event
    public void removeSelectedTower() {
        mapEvents.add(new MapEvent.RemoveTower());

        // refund tower cost
        addMoney(selectedTower.cost / 2);
    }

    // SpawnEnemy event
    private void spawnEnemy(Type type) {
        mapEvents.add(new MapEvent.SpawnEnemy(new Enemy(type, map.getPath())));
    }

    // GETTERS/SETTERS

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

    public int getMoney() {
        return money;
    }

    public void selectTower(Tower tower) {
        selectedTower = tower;
    }

    public void drag(PointF location) {
        dragLocation = location;
    }
}