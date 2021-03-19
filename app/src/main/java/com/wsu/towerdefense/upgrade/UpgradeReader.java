package com.wsu.towerdefense.upgrade;

import android.content.Context;
import android.util.Log;
import com.wsu.towerdefense.Projectile;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Tower;
import com.wsu.towerdefense.Util;
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
 * Each upgrade file contains the upgrades for a single tower. The file must be named with a value from {@link Tower.Type}.
 * <p>
 * File format:
 * <ul>
 *     <li><code>path1</code> : <code>Upgrade</code> array</li>
 *     <li><code>path2</code> : <code>Upgrade</code> array</li>
 * </ul>
 *
 * Upgrade format:
 * <ul>
 *     <li><code>displayName</code> : string - name of the upgrade that is shown to the user</li>
 *     <li><code>description</code> : string - description of the upgrade that is shown to the user</li>
 *     <li><code>cost</code> : int - cost of the upgrade</li>
 *     <li><code>image</code> : string - name of the image file, without extension</li>
 *     <li>
 *         <code>effect</code> : object
 *         <ul>
 *             <li><code>type</code> : <code>Upgrade.Effect</code> - type of upgrade</li>
 *             <li><code>value</code> - value associated with the upgrade</li>
 *         </ul>
 *     </li>
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

    public static Collection<TowerUpgradeData> getUpgrades() {
        return upgrades.values();
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

        List<Upgrade<?>> path1List = new ArrayList<>();
        JSONArray path1 = json.getJSONArray("path1");
        for (int i = 0; i < path1.length(); i++) {
            Upgrade<?> upgrade = parseUpgrade(context, (JSONObject) path1.get(i));
            path1List.add(upgrade);
        }

        List<Upgrade<?>> path2List = new ArrayList<>();
        JSONArray path2 = json.getJSONArray("path2");
        for (int i = 0; i < path2.length(); i++) {
            Upgrade<?> upgrade = parseUpgrade(context, (JSONObject) path2.get(i));
            path2List.add(upgrade);
        }

        return new TowerUpgradeData(
            path1List.toArray(new Upgrade<?>[0]),
            path2List.toArray(new Upgrade<?>[0])
        );
    }

    private static Upgrade<?> parseUpgrade(Context context, JSONObject upgrade)
        throws JSONException, FileNotFoundException {
        String displayName = upgrade.getString("displayName");
        String description = upgrade.getString("description");
        int cost = upgrade.getInt("cost");
        String image = upgrade.getString("image");
        int imageID = Util.getResourceByName(context, image);
        if (imageID == 0) {
            throw new FileNotFoundException("Upgrade image not found");
        }

        JSONObject effectObj = upgrade.getJSONObject("effect");
        Upgrade.Effect effect = Upgrade.Effect.valueOf(effectObj.getString("type"));
        switch (effect) {
            case RANGE:
            case FIRE_RATE:
            case PROJECTILE_VELOCITY:
            case PROJECTILE_DAMAGE: {
                double value = effectObj.getDouble("value");
                return new Upgrade<>(
                    context, displayName, description, cost, imageID, effect, value
                );
            }
            case PROJECTILE: {
                Projectile.Type value = Projectile.Type.valueOf(effectObj.getString("value"));
                return new Upgrade<>(
                    context, displayName, description, cost, imageID, effect, value
                );
            }
            default: {
                return null;
            }
        }
    }
}
