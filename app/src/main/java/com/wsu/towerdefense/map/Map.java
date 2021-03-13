package com.wsu.towerdefense.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.RectF;
import com.wsu.towerdefense.Application;
import java.util.ArrayList;
import java.util.List;

/**
 * A map that has been adjusted for a particular screen size
 */
public class Map extends AbstractMap {

    /**
     * Cached colors for path rectangles drawn in debug mode
     */
    private static final int[] PATH_COLORS = new int[]{
        -65536, -32768, -256, -8323328, -16711936, -16711808,
        -16711681, -16744193, -16776961, -8388353, -65281, -65408
    };

    /**
     * Rectangles representing the path hitbox
     */
    private final List<RectF> bounds;

    public Map(AbstractMap baseMap, int gameWidth, int gameHeight) {
        super(baseMap.name, baseMap.displayName, baseMap.imageID, baseMap.image,
            adjustPath(baseMap.path, gameWidth, gameHeight), baseMap.pathRadius);

        // generate bounds rectangles based on adjusted path
        this.bounds = generateBounds();
    }

    /**
     * Adjust path (scale up) to fit dimensions of the game
     */
    private static List<PointF> adjustPath(List<PointF> path, int gameWidth, int gameHeight) {
        List<PointF> adjustedPath = new ArrayList<>();
        for (PointF point : path) {
            adjustedPath.add(new PointF(
                point.x * gameWidth, point.y * gameHeight
            ));
        }
        return adjustedPath;
    }

    public void render(Canvas canvas, Paint paint) {
        canvas.drawBitmap(this.image, 0, 0, null);

        if (Application.DEBUG) {
            for (int i = 0; i < bounds.size(); i++) {
                paint.reset();
                paint.setColor(PATH_COLORS[i % PATH_COLORS.length]);
                paint.setAlpha(100);
                canvas.drawRect(bounds.get(i), paint);
            }
            for (int i = 0; i < path.size(); i++) {
                PointF point = path.get(i);
                paint.reset();
                paint.setTextSize(75);
                paint.setColor(Color.WHITE);
                paint.setTextAlign(Align.CENTER);
                canvas.drawText(
                    "" + i,
                    point.x,
                    point.y - ((paint.descent() + paint.ascent()) / 2f),
                    paint
                );
            }
        }
    }

    /**
     * Generate bounds rectangles from path
     */
    private List<RectF> generateBounds() {
        List<RectF> tiles = new ArrayList<>();

        for (int i = 1; i < path.size(); i++) {
            PointF p1 = path.get(i - 1);
            PointF p2 = path.get(i);

            // assume straight path
            if (p1.x == p2.x) {
                tiles.add(new RectF(
                    p1.x - pathRadius,
                    Math.min(p1.y, p2.y) - pathRadius,
                    p1.x + pathRadius,
                    Math.max(p1.y, p2.y) + pathRadius
                ));
            } else if (p1.y == p2.y) {
                tiles.add(new RectF(
                    Math.min(p1.x, p2.x) - pathRadius,
                    p1.y - pathRadius,
                    Math.max(p1.x, p2.x) + pathRadius,
                    p1.y + pathRadius
                ));
            }
        }

        return tiles;
    }

    public List<RectF> getBounds() {
        return bounds;
    }
}
