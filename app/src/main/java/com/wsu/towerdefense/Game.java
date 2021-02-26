package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

public class Game extends AbstractGame {

    private List<AbstractMapObject> objects;

    private final Map map;

    public Game(Context context, int displayWidth, int displayHeight) {
        super(context, displayWidth, displayHeight);

        objects = new ArrayList<>();
        map = Map.get("test");

        // Add a single Enemy at the first point of the map
        objects.add(new Enemy(new PointF(map.getPath().get(0)),
                BitmapFactory.decodeResource(getResources(), R.drawable.enemy),
                1000.0f, 0.0f));

        // Add a single tower at the center of the map
        objects.add(new Tower(new PointF(displayWidth / 2, displayHeight / 2),
                BitmapFactory.decodeResource(getResources(), R.drawable.tower)));
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

        for (AbstractMapObject obj : objects) {
            obj.render(lerp, canvas, paint);
        }
    }

    public Map getMap() {
        return map;
    }
}
