package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.List;

public class Game extends AbstractGame {

    private List<AbstractMapObject> objects;

    private final Map map;

    public Game(Context context, int displayWidth, int displayHeight) {
        super(context, displayWidth, displayHeight);

        objects = new ArrayList<>();
        map = Map.get("test");
    }

    @Override
    protected void update(double delta) {
        for (AbstractMapObject obj : objects) {
            obj.update(this, delta);
        }
    }

    @Override
    protected void render(double lerp, Canvas canvas, Paint paint) {
        for (AbstractMapObject obj : objects) {
            obj.render(lerp, canvas, paint);
        }
    }

    public Map getMap() {
        return map;
    }
}
