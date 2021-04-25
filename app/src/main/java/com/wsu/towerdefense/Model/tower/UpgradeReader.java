package com.wsu.towerdefense.Model.tower;

import android.content.Context;
import android.util.Log;
import com.wsu.towerdefense.Model.Projectile;
import com.wsu.towerdefense.Model.tower.Upgrade.Effect;
import com.wsu.towerdefense.Model.tower.Upgrade.StatType;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Util;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Each upgrade file contains the upgrades for a single tower. The file must be named with a value
 * from {@link Tower.Type}.
 * <p>
 * File format:
 * <ul>
 *     <li><code>path1</code> : <code>Upgrade</code> array</li>
 *     <li><code>path2</code> : <code>Upgrade</code> array</li>
 * </ul>
 * <p>
 * Upgrade format:
 * <ul>
 *     <li><code>displayName</code> : string - name of the upgrade that is shown to the user</li>
 *     <li><code>description</code> : string - description of the upgrade that is shown to the user</li>
 *     <li><code>cost</code> : int - cost of the upgrade</li>
 *     <li><code>image</code> : string - name of the image file, without extension</li>
 *     <li>
 *         <code>effect</code> : <code>Effect</code> <em>or</em> <code>effects</code> : <code>Effect</code> array
 *     </li>
 * </ul>
 * <p>
 * Effect format:
 * <ul>
 *     <li><code>type</code> : <code>Upgrade.StatType</code> - stat that is affected</li>
 *     <li><code>value</code> : value associated with the effect</li>
 * </ul>
 */
public class UpgradeReader {

    private static final java.util.Map<Tower.Type, TowerUpgradeData> upgrades = new HashMap<>();

    /**
     * Reads all upgrades from disk and stores them in {@link #upgrades}
     *
     * @throws IOException when 'assets/upgrades/' directory can't be read
     */
    public static void init(Context context) throws IOException {
        String[] files = context.getAssets().list("upgrades");
        for (String fileName : files) {
            try {
                Log.i(context.getString(R.string.logcatKey),
                    "Found upgrade file '" + fileName + "'");
                String data = Util.readFile(context, "upgrades/" + fileName);

                String towerName = fileName.substring(0, fileName.lastIndexOf('.'));
                Tower.Type towerType = Tower.Type.valueOf(towerName);

                TowerUpgradeData towerUpgradeData = parseUpgrades(context, data);
                upgrades.put(towerType, towerUpgradeData);
                Log.i(context.getString(R.string.logcatKey),
                    "Registered upgrades for tower '" + towerName + "'");
            } catch (IOException | JSONException | IllegalArgumentException e) {
                Log.e(context.getString(R.string.logcatKey),
                    "Error while reading upgrades file '" + fileName + "'"
                    , e);
            }
        }
    }

    /**
     * Get the {@link TowerUpgradeData} with the corresponding tower type
     *
     * @param type type of the tower
     * @return corresponding {@link TowerUpgradeData}
     * @throws IllegalArgumentException when tower type <code>type</code> has no upgrades defined
     */
    public static TowerUpgradeData get(Tower.Type type) {
        if (upgrades.containsKey(type)) {
            return upgrades.get(type);
        } else {
            throw new IllegalArgumentException("Invalid tower type '" + type.toString() + "'");
        }
    }

    /**
     * Parse JSON string into {@link TowerUpgradeData} object
     *
     * @param data JSON string of the upgrades
     * @return corresponding {@link TowerUpgradeData}
     * @throws JSONException            when JSON cannot be parsed
     * @throws IllegalArgumentException when upgrade file is invalid
     * @throws FileNotFoundException    when image file is invalid
     */
    private static TowerUpgradeData parseUpgrades(Context context, String data)
        throws JSONException, IllegalArgumentException, FileNotFoundException {
        JSONObject json = new JSONObject(data);

        List<Upgrade> path1 = parsePath(context, json, "path1");
        List<Upgrade> path2 = parsePath(context, json, "path2");
        List<Upgrade> path3 = parsePath(context, json, "path3");

        return new TowerUpgradeData(
            path1.toArray(new Upgrade[0]),
            path2.toArray(new Upgrade[0]),
            path3.toArray(new Upgrade[0])
        );
    }

    private static List<Upgrade> parsePath(Context context, JSONObject json, String key)
        throws JSONException, FileNotFoundException {
        List<Upgrade> pathList = new ArrayList<>();

        JSONArray path = json.getJSONArray(key);
        for (int i = 0; i < path.length(); i++) {
            Upgrade upgrade = parseUpgrade(context, (JSONObject) path.get(i));
            pathList.add(upgrade);
        }

        return pathList;
    }

    private static Upgrade parseUpgrade(Context context, JSONObject upgrade)
        throws JSONException, FileNotFoundException {
        String displayName = upgrade.getString("displayName");
        String description = upgrade.getString("description");
        int cost = upgrade.getInt("cost");
        String image = upgrade.getString("image");
        int imageID = Util.getResourceByName(context, "mipmap", image);
        if (imageID == 0) {
            throw new FileNotFoundException("Upgrade image '" + image + "' not found");
        }
        List<Effect<?>> effects = parseEffects(upgrade);

        return new Upgrade(
            context,
            displayName,
            description,
            cost,
            imageID,
            effects.toArray(new Effect[0])
        );
    }

    private static List<Effect<?>> parseEffects(JSONObject upgrade) throws JSONException {
        List<Effect<?>> effectsList = new ArrayList<>();

        // determine whether effects is an array or an object
        JSONArray effects = upgrade.optJSONArray("effects");

        // array
        if (effects != null) {
            for (int i = 0; i < effects.length(); i++) {
                JSONObject effect = (JSONObject) effects.get(i);
                effectsList.add(parseEffect(effect));
            }
        }
        // object
        else {
            JSONObject effect = upgrade.getJSONObject("effect");
            effectsList.add(parseEffect(effect));
        }

        return effectsList;
    }

    private static Effect<?> parseEffect(JSONObject effect) throws JSONException {
        StatType type = StatType.valueOf(effect.getString("type"));
        switch (type) {
            case RANGE:
            case FIRE_RATE:
            case PROJECTILE_SPEED:
            case PROJECTILE_DAMAGE:
            case PROJECTILE_RANGE:
            case SLOW_TIME:
            case SLOW_RATE:
            case PROJECTILE_PIERCE: {
                float value = (float) effect.getDouble("value");
                return new Effect<>(type, value);
            }
            case SEE_INVISIBLE: {
                boolean value = Boolean.parseBoolean(effect.getString("value"));
                return new Effect<>(type, value);
            }
            case PROJECTILE: {
                Projectile.Type value = Projectile.Type.valueOf(effect.getString("value"));
                return new Effect<>(type, value);
            }
            default: {
                return null;
            }
        }
    }
}
