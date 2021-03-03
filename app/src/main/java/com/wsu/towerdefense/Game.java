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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game extends AbstractGame {

    /**
     * Keeps track of all Towers in the Game
     */
    private final List<Tower> towers;
    /**
     * Keeps track of all Enemies in the Game
     */
    private final List<Enemy> enemies;

    private final Map map;

    private final float towerMenuWidth = 0.1f;    //percent of screen taken up by selectedTowerMenu
    private final float rows = 20f;
    private float cols;
    private PointF cellSize;

    public Game(Context context, int displayWidth, int displayHeight) {
        super(context, displayWidth, displayHeight);

        cellSize = getCellSize();

        towers = new ArrayList<>();
        enemies = new ArrayList<>();

        map = MapReader.get("map1");
        map.generateTiles(cellSize);

        Log.i(context.getString(R.string.logcatKey),
                "Started game with map '" + map.getName() + "'");

        setup();
        Log.i(context.getString(R.string.logcatKey), "Set up game objects");
    }

    @Override
    protected void update(double delta) {

        // Update the Enemies, remove any dead Enemies
        for (Iterator<Enemy> enemyIt = enemies.iterator(); enemyIt.hasNext(); ) {
            Log.i("--enemy   --", "processing enemy");
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
            addEnemy(200, 1000);
            addEnemy(200, 1000);
            addEnemy(200, 1000);
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

    protected PointF getCellSize () {
        float screenXActual = (float) getDisplayWidth() * (1f - towerMenuWidth);
        float screenYActual = getDisplayHeight();
        cols = rows * (screenXActual / screenYActual);
        float y = screenXActual / cols;
        float x = getDisplayHeight() / rows;

        Log.i("--cellsize --", x + " " + y);
        return (new PointF(x, y));
    }

    private void drawGridLines (Canvas canvas, Paint paint){
        paint.setColor(Color.RED);
        for (float i = 0; i < rows; i++) {
//            Log.i("--draw grid--", "Drawing row: " + i + " at " +
//                    0 + " " + i * cellSize.y + " " + cols * cellSize.x + " " + i * cellSize.y);
            canvas.drawLine(0, i * cellSize.y,
                    cols * cellSize.x, i * cellSize.y, paint);
        }

        for (float i = 0; i < cols; i++) {
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

    /**
     * A method used to set up some of the Game's objects.
     */
    private void setup () {
        //TESTING
        // Add Towers
        towers.add(new Tower(new PointF(1000, 580),
                BitmapFactory.decodeResource(getResources(), R.drawable.tower), 384,
                BitmapFactory.decodeResource(getResources(), R.drawable.projectile), 750f, 10));
        towers.add(new Tower(new PointF(1400, 860),
                BitmapFactory.decodeResource(getResources(), R.drawable.tower), 384,
                BitmapFactory.decodeResource(getResources(), R.drawable.projectile), 750f, 10));

        //TESTING
        // Add Enemies
        addEnemy(200, 1000);
        addEnemy(200, 1000);
        addEnemy(200, 1000);
    }

    /**
     * A helper method to reduce complexity and size of main Game methods (update, setup)
     *
     * @param velocity  Enemy velocity
     * @param hp        Enemy hp
     */
    private void addEnemy ( float velocity, int hp){
        enemies.add(new Enemy(map.getPath(), cellSize,
                BitmapFactory.decodeResource(getResources(), R.drawable.enemy),
                200, 1000));
    }
}

