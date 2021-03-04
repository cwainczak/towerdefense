package com.wsu.towerdefense.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import com.wsu.towerdefense.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Map {

    /**
     * Name of the map
     */
    private final String name;

    /**
     * List of points representing the path enemies can take, in tile units
     */
    private final List<Point> path;
    private List<RectF> tiles;

    Map(String name, List<Point> path) {
        this.name = name;
        this.path = path;

        this.tiles = new ArrayList<>();
    }

    public void render(Canvas canvas, Paint paint){
        for( RectF tile : tiles){
            canvas.drawRect(tile, paint);
        }
    }

    /**
     * Generates a RectF of size cellSize for each cell along the path
     * Note: only works with horizontal and vertical movement. Angled movement will not work
     *
     * @param cellSize size of each cell within the grid
     */
    public void generateTiles(PointF cellSize){
        int x;
        int y;

        for(int i = 0; i < path.size() -1; i++) {
            x = path.get(i).x;
            y = path.get(i).y;

            while (x != path.get(i + 1).x || y != path.get(i + 1).y) {
                //add rect at cell
                tiles.add(new RectF(x * cellSize.x, y * cellSize.y,
                          (x * cellSize.x) + cellSize.x,
                        (y * cellSize.y) + cellSize.y));

                //if x is not at next point, determine direction and increment x
                if (x != path.get(i + 1).x) {
                    if (path.get(i + 1).x - x > 0) {        // x needs to move right
                        x++;
                    } else {                                // x needs to move left
                        x--;
                    }
                    //if y is not at next point, determine direction and increment y
                } else {                                    // y needs to move towards point
                    if (path.get(i + 1).y - y > 0) {        // y needs to move down
                        y++;
                    } else {                                // y needs to move up
                        y--;
                    }
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public List<Point> getPath() {
        return Collections.unmodifiableList(path);
    }
}
