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
import java.util.List;

public class Game extends AbstractGame {

    private final List<AbstractMapObject> objects;

    private final Map map;

    private final float towerMenuWidth = 0.1f;    //percent of screen taken up by selectedTowerMenu
    private final float rows = 20f;
    private float cols;
    private PointF cellSize;

    public Game(Context context, int displayWidth, int displayHeight) {
        super(context, displayWidth, displayHeight);

        cellSize = getCellSize();

        objects = new ArrayList<>();
        map = MapReader.get("map1");
        map.generateTiles(cellSize);

        // Add a single Enemy at the first point of the map
        objects.add(new Enemy(map.getPath(), cellSize,
                BitmapFactory.decodeResource(getResources(), R.drawable.enemy), 500f));

        // Add a single tower at the center of the map
        objects.add(new Tower(new PointF(displayWidth / 2, displayHeight / 2),
                BitmapFactory.decodeResource(getResources(), R.drawable.tower)));


        Log.i(context.getString(R.string.logcatKey), "Started game with map '" + map.getName() + "'");
    }

    @Override
    protected void update(double delta) {
        for (AbstractMapObject obj : objects) {
            obj.update(this, delta);
        }
    }

    @Override
    protected void render(double lerp, Canvas canvas, Paint paint) {
        // Draw the background
        canvas.drawColor(Color.BLACK);

        paint.setColor(Color.YELLOW);
        map.render(canvas, paint);

        drawGridLines(canvas, paint);

        for (AbstractMapObject obj : objects) {
            obj.render(lerp, canvas, paint);
        }
    }

    protected PointF getCellSize() {
        float screenXActual = (float)getDisplayWidth() * (1f - towerMenuWidth);
        float screenYActual = getDisplayHeight();
        cols = rows * (screenXActual / screenYActual);
        float y = screenXActual  / cols;
        float x = getDisplayHeight() / rows;

        return (new PointF(x, y));
    }

    private void drawGridLines(Canvas canvas, Paint paint){
        paint.setColor(Color.RED);
        for(float i = 0; i < rows; i++){
//            Log.i("--draw grid--", "Drawing row: " + i + " at " +
//                    0 + " " + i * cellSize.y + " " + cols * cellSize.x + " " + i * cellSize.y);
            canvas.drawLine(0, i * cellSize.y,
                    cols * cellSize.x, i * cellSize.y, paint);
        }

        for(float i = 0; i < cols; i++){
//            Log.i("--draw grid--", "Drawing col: " + i + " at " +
//                    i * cellSize.x + " " + 0 + " " + i * cellSize.x + " " + rows * cellSize.y);
            canvas.drawLine(i * cellSize.x, 0,
                    i * cellSize.x, rows * cellSize.y, paint);
        }
    }

    public Map getMap() {
        return map;
    }
}
