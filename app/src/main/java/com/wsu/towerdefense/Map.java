package com.wsu.towerdefense;

import android.graphics.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Map {

    /**
     * List of points representing the path enemies can take, in tile units
     */
    private final List<Point> path;

    private Map(List<Point> path) {
        this.path = path;
    }

    /**
     * Get the {@link Map} with the corresponding <code>name</code>
     *
     * @param name name of the map to get
     * @return corresponding {@link Map}
     */
    public static Map get(String name) {
        // in future updates, this method will read a map from a file; for now it has test data

        List<Point> path = new ArrayList<>();
        path.add(new Point(0, 3));
        path.add(new Point(1, 3));
        path.add(new Point(1, 1));
        path.add(new Point(3, 1));
        path.add(new Point(3, 5));
        path.add(new Point(5, 5));
        path.add(new Point(5, 3));
        path.add(new Point(6, 3));

        return new Map(path);
    }

    public List<Point> getPath() {
        return Collections.unmodifiableList(path);
    }
}
