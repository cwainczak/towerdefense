package com.wsu.towerdefense.map;

import android.graphics.Point;
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

    Map(String name, List<Point> path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public List<Point> getPath() {
        return Collections.unmodifiableList(path);
    }
}
