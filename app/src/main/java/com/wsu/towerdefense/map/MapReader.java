package com.wsu.towerdefense.map;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import com.wsu.towerdefense.R;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Map format:
 * <ul>
 *     <li><code>displayName</code> : string - name of the map that is shown to the user</li>
 *     <li><code>image</code> : string - name of the image file, without extension</li>
 *     <li><code>pathRadius</code> : integer - width of the path, in pixels; used to calculate bounds</li>
 *     <li><code>path</code> : array - list of objects <code>{x:double, y:double}</code> representing the path, coordinates normalized from 0 to 1</li>
 * </ul>
 */
public class MapReader {

    private static final java.util.Map<String, AbstractMap> maps = new HashMap<>();

    /**
     * Reads all maps from disk and stores them in {@link #maps}
     *
     * @throws IOException when 'assets/maps/' directory can't be read
     */
    public static void init(Context context) throws IOException {
        String[] mapFiles = context.getAssets().list("maps");
        for (String mapFile : mapFiles) {
            try {
                Log.i(context.getString(R.string.logcatKey), "Found map file '" + mapFile + "'");
                String mapName = mapFile.substring(0, mapFile.lastIndexOf('.'));

                String data = readFile(context, "maps/" + mapFile);

                AbstractMap map = parseMap(context, mapName, data);
                maps.put(mapName, map);
                Log.i(context.getString(R.string.logcatKey), "Registered map '" + mapName + "'");
            } catch (IOException | JSONException | IllegalArgumentException e) {
                Log.e(context.getString(R.string.logcatKey),
                    "Error while reading map file '" + mapFile + "'"
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

    /**
     * Read a file from the assets directory
     *
     * @param context  context
     * @param fileName file within 'assets/' to read
     * @return contents of the file
     * @throws IOException when file is invalid
     */
    private static String readFile(Context context, String fileName) throws IOException {
        InputStream stream = context.getAssets().open(fileName);

        String data = new BufferedReader(new InputStreamReader(stream)).lines()
            .collect(Collectors.joining("\n"));

        return data;
    }
}
