package com.wsu.towerdefense;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Each wave is split up into sets. Each set contains an Enemy.Type, amount of enemies to be
 * spawned, and a delay to wait before spawning the next enemy.
 *
 */
public class Waves {

    private int waves;
    private int wave = 0;
    private int set = 0;
    private int spawned = 0;
    double timeSinceSpawn = 0.0;

    boolean running = false;

    List<List<Integer>> amounts;
    List<List<Double>> delays;
    List<List<Enemy.Type>> types;

    public Waves(Context context){
        amounts = new ArrayList<>();
        delays = new ArrayList<>();
        types = new ArrayList<>();

        try {
            parseWaves(context, "waves/standard.json");
        }
        catch (IOException | JSONException e) {
            Log.e(context.getString(R.string.logcatKey), "Error while initializing Waves", e);
        }
    }

    /**
     * alternate constructor for manually passing List values. Primarily used for testing
     */
    public Waves(List<List<Integer>> amounts,
                 List<List<Double>> delays,
                 List<List<Enemy.Type>> types,
                 int waves
    ){
        this.amounts = amounts;
        this.delays = delays;
        this.types = types;
        this.waves = waves;
    }

    public void parseWaves(Context context, String fileName) throws JSONException, IOException {
        InputStream stream = context.getAssets().open(fileName);

        String data = new BufferedReader(new InputStreamReader(stream)).lines()
                .collect(Collectors.joining("\n"));

        JSONObject waveReader = new JSONObject(data);

        JSONArray a = waveReader.getJSONArray("amounts");
        JSONArray d = waveReader.getJSONArray("delays");
        JSONArray t = waveReader.getJSONArray("types");

        waves = a.length();

        // it is assumed that all arrays within amounts, delays, and types are of the same size
        for (int i = 0; i < a.length(); i++) {
            amounts.add(new ArrayList<>());
            delays.add(new ArrayList<>());
            types.add(new ArrayList<>());

            for (int j = 0; j < a.getJSONArray(i).length(); j++) {
                amounts.get(i).add(a.getJSONArray(i).getInt(j));
                delays.get(i).add(d.getJSONArray(i).getDouble(j));
                types.get(i).add(Enemy.Type.valueOf(t.getJSONArray(i).getString(j)));
            }
        }
    }

    //potential issue: running out of waves results in fall through to elseif
    private void progressWave(){
        spawned++;

        //if all enemies have been spawned in the set
        if(spawned - amounts.get(wave).get(set) >= 0){
            spawned = 0;
            set++;
        }
        //if all sets in a wave have passed and there are more waves
        if(set - amounts.get(wave).size() >= 0 &&
                wave < waves){
            Log.i("incrementing wave: ", wave + " -> " + wave + 1);
            wave++;
            set = 0;
            running = false;
        }
    }

    /**
     * @return current enemyType and progresses Wave
     */
    public Enemy.Type next(){
        Enemy.Type tempType = types.get(wave).get(set);
        progressWave();
        return tempType;
    }

    public void updateTimeSinceSpawn(double delta){
        timeSinceSpawn += delta;
    }

    public boolean delayPassed(){
        if(timeSinceSpawn >= delays.get(wave).get(set)){
            timeSinceSpawn = 0;
            return true;
        }
        else{
            return false;
        }
    }

    public int getWave() {
        return wave + 1;
    }

    public boolean isRunning(){
        return running;
    }

    public void setRunning(boolean running){
        this.running = running;
    }
}
