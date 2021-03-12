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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game extends AbstractGame implements Serializable {

    private static final int START_LIVES = 5;

    /**
     * Keeps track of all Towers in the Game
     */
    private List<Tower> towers;
    /**
     * Keeps track of all Enemies in the Game
     */
    private transient final List<Enemy> enemies;

    private Map map;

    public static final float towerRadius = 56; //radius of tower object using tower.png

    private int lives;

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

    public Game(Context context, int gameWidth, int gameHeight, SaveState saveState,
        String mapName) {
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
        towers = hasSave ? saveState.towers : new ArrayList<>();
        lives = hasSave ? saveState.lives : START_LIVES;

        enemies = new ArrayList<>();

        Log.i(context.getString(R.string.logcatKey),
            "Started game with map '" + map.getName() + "'"
        );
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

    @Override
    protected void update(double delta) {
        // Update the Enemies, remove any dead Enemies
        for (Iterator<Enemy> enemyIt = enemies.iterator(); enemyIt.hasNext(); ) {
            Enemy e = enemyIt.next();

            if (e.isAlive()) {
                e.update(this, delta);

                // If the Enemy reached the end of the path
                if (e.isAtPathEnd()) {
                    // Remove a life
                    lives--;

                    // Enemy is off-screen, can be removed
                    enemyIt.remove();

                    // Game over if out of lives
                    if (lives <= 0) {
                        gameOver();
                        return;
                    }
                }

            } else {
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
            t.render(lerp, canvas, paint);

            // Draw all tower ranges if in debug mode
            if (Application.DEBUG) {
                t.drawRange(canvas, paint);
                t.drawLine(canvas, paint);
            } else if (selectedTower == t) {
                t.drawRange(canvas, paint);
            }
        }

        for (Enemy e : enemies) {
            e.render(lerp, canvas, paint);
        }

        drawLives(canvas, paint);
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
     * Draws the life count to the top left corner of the canvas
     *
     * @param canvas Canvas to draw the life count on
     * @param paint  Paint to draw with
     */
    private void drawLives(Canvas canvas, Paint paint) {
        int posX = 10;
        int posY = 60;

        paint.reset();
        paint.setColor(Color.WHITE);
        paint.setShadowLayer(0.1f, 5, 5, Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(75);

        canvas.drawText("Lives: " + lives, posX, posY, paint);
    }

    /**
     * Place a tower at given coordinates if placement is valid
     *
     * @param x x
     * @param y y
     * @return whether position is valid
     */
    public boolean placeTower(float x, float y) {
        // validate here
        if (isValidPlacement(new PointF(x, y))) {
            addBuffer = new Tower(new PointF(x, y), 384, 750f, 10);
            return true;
        }

        return false;
    }

    public void removeSelectedTower() {
//        selectedTower.isSelected = false;
        removeTower = true;
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
            if (rect.left <= location.x && location.x <= rect.right &&
                rect.top <= location.y && location.y <= rect.bottom) {
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

    public void spawnEnemies() {
        save();

        for (int i = 0; i < 3; i++) {
            enemies.add(new Enemy(map.getPath(), 40, 350 + 50 * i));
        }
    }

    public void setSelectedTower(Tower tower) {
        selectedTower = tower;
    }
}