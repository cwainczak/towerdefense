package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

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

    private final float towerMenuWidth = 0.1f;    //percent of screen taken up by selectedTowerMenu
    private final float rows = 20f;
    private float cols;
    private PointF cellSize;

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
        }
        // default state
        else {
            map = MapReader.get("map1");

            // TESTING
            towers.add(new Tower(new PointF(1000, 580), 384, 750f, 10));
            towers.add(new Tower(new PointF(1400, 860), 384, 750f, 10));
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

            } else {
                // Remove dead Enemies
                enemyIt.remove();
            }
        }

        // Update the Towers
        for (Tower t : towers) {
            t.update(this, delta);
        }

        //TESTING
        // Add enemies whenever all enemies are killed
        if (enemies.size() == 0) {
            spawnTestEnemies();
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
    }


    /**
     * Calculates the number of columns based on screen dimensions and space reserved for
     * towerMenu, then calculates the height and width of each cell within the grid
     *
     * @return PointF containing the width and height of each cell within the grid
     */
    protected PointF getCellSize () {
        float screenXActual = (float) getDisplayWidth() * (1f - towerMenuWidth);
        float screenYActual = getDisplayHeight();
        cols = rows * (screenXActual / screenYActual);
        float y = screenXActual / cols;
        float x = getDisplayHeight() / rows;

        return (new PointF(x, y));
    }

    /**
     * draws the grid onto the screen for debugging purposes
     *
     * @param canvas    Canvas to draw the lines on
     * @param paint     Paint to draw the lines with
     */
    private void drawGridLines (Canvas canvas, Paint paint){
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

    public Map getMap () {
        return map;
    }

    public List<Enemy> getEnemies () {
        return enemies;
    }

    public List<Tower> getTowers() {
        return towers;
    }

    private void spawnTestEnemies() {
        enemies.add(new Enemy(map.getPath(), cellSize, 40, 500));
        enemies.add(new Enemy(map.getPath(), cellSize, 40, 450));
        enemies.add(new Enemy(map.getPath(), cellSize, 40, 400));
    }
}