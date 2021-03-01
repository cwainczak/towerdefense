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

    private final List<AbstractMapObject> objects;
    private final List<Tower> towers;
    private final List<Enemy> enemies;

    private final Map map;

    public Game(Context context, int displayWidth, int displayHeight) {
        super(context, displayWidth, displayHeight);

        objects = new ArrayList<>();
        towers = new ArrayList<>();
        enemies = new ArrayList<>();

        map = MapReader.get("map1");

        Log.i(context.getString(R.string.logcatKey), "Started game with map '" + map.getName() + "'");

        setup();
    }

    @Override
    protected void update(double delta) {

        // Update the Towers
        for (Tower t : towers) {
            t.update(this, delta);
        }

        // Update the Enemies
        for (Iterator<Enemy> enemyIt = enemies.iterator(); enemyIt.hasNext(); ) {
            Enemy e = enemyIt.next();
            e.update(this, delta);

            // If Enemy dies
            if (!e.isAlive) {
                enemyIt.remove();
            }
        }

        // Update anything else
        for (AbstractMapObject obj : objects) {
            obj.update(this, delta);
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

        // Draw anything else
        for (AbstractMapObject obj : objects) {
            obj.render(lerp, canvas, paint);
        }
    }

    public Map getMap() {
        return map;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    /**
     * Used to set up some of the Game's objects.
     */
    private void setup() {

        // Currently used for testing purposes
        // Add Enemies
        enemies.add(new Enemy(new PointF(200, 720),
                BitmapFactory.decodeResource(getResources(), R.drawable.enemy),
                300.0f, 0.0f));
        enemies.add(new Enemy(new PointF(600, 720),
                BitmapFactory.decodeResource(getResources(), R.drawable.enemy),
                300.0f, 0.0f));

        // Add Towers
        towers.add(new Tower(new PointF(1000, 580),
                BitmapFactory.decodeResource(getResources(), R.drawable.tower), 384,
                BitmapFactory.decodeResource(getResources(), R.drawable.projectile), 10));
        towers.add(new Tower(new PointF(1400, 860),
                BitmapFactory.decodeResource(getResources(), R.drawable.tower), 384,
                BitmapFactory.decodeResource(getResources(), R.drawable.projectile), 10));
    }
}
