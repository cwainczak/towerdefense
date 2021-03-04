package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
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

    /**
     * Keeps track of all Towers in the Game
     */
    private List<Tower> towers;
    /**
     * Keeps track of all Enemies in the Game
     */
    private transient final List<Enemy> enemies;

    private Map map;

    //private final float towerMenuWidth = 0.1f;    //percent of screen taken up by selectedTowerMenu
    private final float rows = 20f;
    private float cols;
    private PointF cellSize;

    private int lives;

    /**
     * The next tower to be added; prevents {@link java.util.ConcurrentModificationException} when
     * iterating
     */
    private Tower buffer = null;

    public Game(Context context, int displayWidth, int displayHeight, SaveState saveState) {
        super(context, displayWidth, displayHeight);

        cellSize = getCellSize();

        towers = new ArrayList<>();
        enemies = new ArrayList<>();

        init(context, saveState);
        map.generateTiles(cellSize);

        Log.i(context.getString(R.string.logcatKey),
            "Started game with map '" + map.getName() + "'"
        );
    }

    private void init(Context context, SaveState saveState) {
        // load save
        if (saveState != null) {
            Log.i(context.getString(R.string.logcatKey),
                "Loading save file '" + saveState.saveFile + "'"
            );

            map = MapReader.get(saveState.mapName);
            towers = saveState.towers;
            lives = saveState.lives;
        }
        // default state
        else {
            map = MapReader.get("map1");

            lives = 5;

            // TESTING
            spawnTestTowers();
            spawnTestEnemies();
        }
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

            if (e.isAlive) {
                e.update(this, delta);

                // If the Enemy reached the end of the path
                if (e.isAtPathEnd) {
                    // Remove a life
                    lives--;

                    // Enemy is off-screen, can be removed
                    enemyIt.remove();

                    // Game over if out of lives
                    if (lives <= 0) {
                        gameOver();
                        break;
                    }
                }

            } else {
                // Remove dead Enemies
                enemyIt.remove();
            }
        }

        // add tower from buffer
        if (buffer != null) {
            towers.add(buffer);
            buffer = null;
        }
        // Update the Towers
        for (Tower t : towers) {
            t.update(this, delta);
        }

        //TESTING
        // Add enemies whenever all enemies are killed
        if (enemies.size() == 0) {
            spawnTestEnemies();
            // save game when "wave ends"
            save();
        }
    }

    @Override
    protected void render(double lerp, Canvas canvas, Paint paint) {
        // Draw the background
        canvas.drawColor(Color.BLACK);

        paint.setColor(Color.YELLOW);
        map.render(canvas, paint);

        drawGridLines(canvas, paint);

        // Draw the Towers
        for (Tower t : towers) {
            t.render(lerp, canvas, paint);
        }

        // Draw the Enemies
        for (Enemy e : enemies) {
            e.render(lerp, canvas, paint);
        }

        // Draw the lives
        drawLives(canvas, paint);
    }


    /**
     * Calculates the number of columns based on screen dimensions and space reserved for towerMenu,
     * then calculates the height and width of each cell within the grid
     *
     * @return PointF containing the width and height of each cell within the grid
     */
    protected PointF getCellSize() {
        cols = rows * ((float) getGameWidth() / getGameHeight());
        float y = getGameWidth() / cols;
        float x = getGameHeight() / rows;

        return (new PointF(x, y));
    }

    /**
     * draws the grid onto the screen for debugging purposes
     *
     * @param canvas Canvas to draw the lines on
     * @param paint  Paint to draw the lines with
     */
    private void drawGridLines(Canvas canvas, Paint paint) {
        paint.setColor(Color.RED);
        for (int i = 0; i < rows; i++) {
//            Log.i("--draw grid--", "Drawing row: " + i + " at " +
//                    0 + " " + i * cellSize.y + " " + cols * cellSize.x + " " + i * cellSize.y);
            canvas.drawLine(0, i * cellSize.y,
                cols * cellSize.x, i * cellSize.y, paint);
        }

        for (int i = 0; i < cols; i++) {
//            Log.i("--draw grid--", "Drawing col: " + i + " at " +
//                    i * cellSize.x + " " + 0 + " " + i * cellSize.x + " " + rows * cellSize.y);
            canvas.drawLine(i * cellSize.x, 0,
                i * cellSize.x, rows * cellSize.y, paint);
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

        paint.setColor(Color.WHITE);
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
        buffer = new Tower(new PointF(x, y), 384, 750f, 5);

        return true;
    }

    /**
     * Ends this game and returns to the the menu
     */
    private void gameOver() {
        running = false;
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

    private void spawnTestTowers() {
        towers.add(new Tower(new PointF(800, 580), 384, 750f, 5));
        towers.add(new Tower(new PointF(720, 1000), 384, 750f, 5));
    }

    private void spawnTestEnemies() {
        for (int i = 0; i < 3; i++) {
            enemies.add(new Enemy(map.getPath(), cellSize, 40, 350 + 50 * i));
        }
    }
}