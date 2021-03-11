package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    private static final int VALID_RANGE_COLOR = Color.argb(255, 80, 80, 80);
    private static final int INVALID_RANGE_COLOR = Color.argb(255, 180, 0, 0);
    private static final int RANGE_OPACITY = 90;

    /**
     * Keeps track of all Towers in the Game
     */
    private List<Tower> towers;
    /**
     * Keeps track of all Enemies in the Game
     */
    private transient final List<Enemy> enemies;

    private Map map;

    private final float rows = 20f;
    private float cols;
    private final PointF cellSize;

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

    public PointF dragLocation = null;

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
        // Draw the background
        canvas.drawColor(Color.BLACK);

        // Draw debug information
        if (Application.DEBUG) {
            map.render(canvas, paint);
            drawGridLines(canvas, paint);
        }

        // Draw the Towers
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

        // Draw the Enemies
        for (Enemy e : enemies) {
            e.render(lerp, canvas, paint);
        }

        // Draw the lives
        drawLives(canvas, paint);

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
        paint.setStrokeWidth(1);
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
        for (Tower tower : getTowers()) {
            //calculate distance
            double distance = distanceToPoint(location, tower.getLocation());

            //calculate minDistance : assumes half of width is radius of tower
            double minDistance = towerRadius * 2;

            //determine if new tower is too close
            if (distance < minDistance) {
                return false;
            }
        }

        for (RectF tile : map.getTiles()) {
            //calculate distance
            double distance = distanceToPoint(location, new PointF(tile.centerX(), tile.centerY()));

            //calculate minDistance : with tileRadius corners of tiles will not affect calculation
            float tileRadius = tile.centerX() - tile.left;
            double minDistance = towerRadius + tileRadius;

            //determine if new tower is too close
            if (distance < minDistance) {
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
            enemies.add(new Enemy(map.getPath(), cellSize, 40, 350 + 50 * i));
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
}