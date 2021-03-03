package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

    public Game(Context context, int displayWidth, int displayHeight) {
        super(context, displayWidth, displayHeight);

        towers = new ArrayList<>();
        enemies = new ArrayList<>();

        map = MapReader.get("map1");

        Log.i(context.getString(R.string.logcatKey),
            "Started game with map '" + map.getName() + "'");

        setup();
        Log.i(context.getString(R.string.logcatKey), "Set up game objects");
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
            addEnemy(200, 320, 300, 0, 100);
            addEnemy(2000, 720, 300, 0, 100);
            addEnemy(200, 1120, 300, 0, 100);
        }

    }

    @Override
    protected void render(double lerp, Canvas canvas, Paint paint) {
        // Draw the background (light brown)
        canvas.drawColor(Color.parseColor("#BD9A7A"));

        // Draw the Towers
        for (Tower t : towers) {
            t.render(lerp, canvas, paint);
        }

        // Draw the Enemies
        for (Enemy e : enemies) {
            e.render(lerp, canvas, paint);
        }
    }

    public Map getMap() {
        return map;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    /**
     * A method used to set up some of the Game's objects.
     */
    private void setup() {
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
        addEnemy(200, 320, 300, 0, 100);
        addEnemy(2000, 720, 300, 0, 100);
        addEnemy(200, 1120, 300, 0, 100);
    }

    /**
     * A helper method to reduce complexity and size of main Game methods (update, setup)
     *
     * @param x         Enemy center x location
     * @param y         Enemy center y location
     * @param velocityX Velocity in the x direction
     * @param velocityY Velocity in the y direction
     */
    private void addEnemy(float x, float y, float velocityX, float velocityY, int hp) {
        enemies.add(new Enemy(new PointF(x, y),
            BitmapFactory.decodeResource(getResources(), R.drawable.enemy),
            velocityX, velocityY, hp));
    }
}
