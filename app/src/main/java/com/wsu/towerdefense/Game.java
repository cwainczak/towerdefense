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

    public Game(Context context, int displayWidth, int displayHeight, SaveState saveState) {
        super(context, displayWidth, displayHeight);

        towers = new ArrayList<>();
        enemies = new ArrayList<>();

        init(context, saveState);

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

    public List<Tower> getTowers() {
        return towers;
    }

    private void spawnTestEnemies() {
        enemies.add(new Enemy(new PointF(200, 320), 300, 0, 100));
        enemies.add(new Enemy(new PointF(2000, 720), 300, 0, 100));
        enemies.add(new Enemy(new PointF(200, 1120), 300, 0, 100));
    }
}
