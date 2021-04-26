package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import com.wsu.towerdefense.map.AbstractMap;
import com.wsu.towerdefense.map.Map;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * File format:
 * <ul>
 *     <li><code>displayName</code> : string - name of the map that is shown to the user</li>
 *     <li><code>image</code> : string - name of the image file, without extension</li>
 *     <li><code>pathRadius</code> : integer - width of the path, in pixels; used to calculate bounds</li>
 *     <li><code>path</code> : array - list of objects <code>{x:double, y:double}</code> representing the path, coordinates normalized from 0 to 1</li>
 * </ul>
 */
public class MapReader {

    private static final String MAPS_DIR = "maps";

    private static final java.util.Map<String, AbstractMap> maps = new HashMap<>();

    /**
     * Reads all maps from disk and stores them in {@link #maps}
     *
     * @throws IOException when 'assets/maps/' directory can't be read
     */
    public static void init(Context context) throws IOException {
        String[] files = context.getAssets().list(MAPS_DIR);
        for (String fileName : files) {
            try {
                Log.i(context.getString(R.string.logcatKey), "Found map file '" + fileName + "'");
                String data = Util.readFile(context, MAPS_DIR + "/" + fileName);

                String mapName = fileName.substring(0, fileName.lastIndexOf('.'));

                AbstractMap map = parseMap(context, mapName, data);
                maps.put(mapName, map);
                Log.i(context.getString(R.string.logcatKey), "Registered map '" + mapName + "'");
            } catch (IOException | JSONException | IllegalArgumentException e) {
                Log.e(context.getString(R.string.logcatKey),
                    "Error while reading map file '" + fileName + "'"
                    , e);
            }
        }
    }

    /**
     * Get the {@link Map} with the corresponding <code>name</code>
     *
     * @param name name of the map to get
     * @return corresponding {@link Map}
     * @throws IllegalArgumentException when map called <code>name</code> cannot be found
     */
    public static AbstractMap get(String name) {
        if (maps.containsKey(name)) {
            return maps.get(name);
        } else {
            throw new IllegalArgumentException("Invalid map '" + name + "'");
        }
    }

    public static Collection<AbstractMap> getMaps() {
        return maps.values();
    }

    /**
     * Parse JSON string into {@link Map} object
     *
     * @param name name of the map
     * @param data JSON string of the map
     * @return corresponding {@link Map}
     * @throws JSONException            when JSON cannot be parsed
     * @throws IllegalArgumentException when map is invalid
     * @throws FileNotFoundException    when image file is invalid
     */
    private static AbstractMap parseMap(Context context, String name, String data)
        throws JSONException, IllegalArgumentException, FileNotFoundException {
        JSONObject json = new JSONObject(data);

        String displayName = json.getString("displayName");
        String imageName = json.getString("image");
        float pathRadius = (float) json.getDouble("pathRadius");

        List<PointF> pathList = new ArrayList<>();

        JSONArray path = json.getJSONArray("path");
        for (int i = 0; i < path.length(); i++) {
            JSONObject point = (JSONObject) path.get(i);
            float x = (float) point.getDouble("x");
            float y = (float) point.getDouble("y");
            pathList.add(new PointF(x, y));
        }

        if (pathList.size() < 2) {
            throw new IllegalArgumentException("Map path must have at least two points");
        }

        int imageID = context.getResources().getIdentifier(
            imageName,
            "mipmap",
            context.getPackageName()
        );
        if (imageID == 0) {
            throw new FileNotFoundException("Map image not found");
        }

        return new AbstractMap(context, name, displayName, imageID, pathList, pathRadius);
    }
}
