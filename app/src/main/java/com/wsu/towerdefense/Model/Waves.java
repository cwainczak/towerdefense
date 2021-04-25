package com.wsu.towerdefense.Model;

import android.content.Context;
import android.util.Log;

import com.wsu.towerdefense.R;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Each wave is split up into sets. Each set contains an Enemy.Type, amount of enemies to be
 * spawned, and a delay to wait before spawning the next enemy.
 *
 */
public class Waves implements Serializable {

    private int maxWaves;
    private int wavesToWin;
    private int curWave = 0;
    private int curSet = 0;
    private int spawnedThisSet = 0;
    double timeSinceSpawn = 0.0;

    boolean running = false;
    boolean wavesEnded = false;
    boolean setStarted = false;
    boolean gameEnded = false;

    List<List<Integer>> amounts;
    List<List<Double>> delays;
    List<List<Double>> setDelays;
    List<List<Enemy.Type>> types;

    /**
     * Constructor which uses {@link #parseWaves(Context, String)} to populate List values
     *
     * @param context   Used to access files
     */
    public Waves(Context context, Game.Difficulty difficulty){
        amounts = new ArrayList<>();
        delays = new ArrayList<>();
        setDelays = new ArrayList<>();
        types = new ArrayList<>();
        wavesToWin = difficulty.waves;

        try {
            parseWaves(context, "waves/standard.json");
        }
        catch (IOException | JSONException e) {
            Log.e(context.getString(R.string.logcatKey), "Error while initializing Waves", e);
        }
    }

    /**
     * alternate constructor for manually passing Lists. Primarily used for testing
     */
    public Waves(List<List<Integer>> amounts,
                 List<List<Double>> delays,
                 List<List<Enemy.Type>> types,
                 int wavesToWin
    ){
        this.amounts = amounts;
        this.delays = delays;
        this.types = types;
        this.wavesToWin = wavesToWin;
    }

    public void update(Game game, double delta){
        if(isRunning()) {
            updateTimeSinceSpawn(delta);

            if ((setStarted || setDelayPassed()) && delayPassed()) {
                    game.spawnEnemy(next());
            }
        }
    }

    /**
     * Parse JSON file to populate amounts, delays, types, and waves
     *
     * Waves format:
     * Each array within <code>amounts</code>, <code>delays</code>, and <code>waves</code>
     * represents a wave. each value within the arrays represents a set.
     * <ul>
     *     <li><code>amounts</code> : The amount of enemies that must be spawned before the set is incremented. </li>
     *     <li><code>delays</code> : The amount of time which must pass between spawning each enemy for a given set.</li>
     *     <li><code>types</code> : The type of enemy to spawn for a given set</li>
     * </ul>
     *
     * @param context   Used to access files through context.getAssets
     * @param fileName  String name of the JSON file to be read into a buffer
     * @throws JSONException    If issue calling get methods of JSONObject
     * @throws IOException      If File fileName does ot exist
     */
    public void parseWaves(Context context, String fileName) throws JSONException, IOException {
        InputStream stream = context.getAssets().open(fileName);

        String data = new BufferedReader(new InputStreamReader(stream)).lines()
                .collect(Collectors.joining("\n"));

        JSONObject waveReader = new JSONObject(data);

        JSONArray a = waveReader.getJSONArray("amounts");
        JSONArray d = waveReader.getJSONArray("delays");
        JSONArray sd = waveReader.getJSONArray("set_delays");
        JSONArray t = waveReader.getJSONArray("types");

        maxWaves = a.length();

        // it is assumed that all arrays within amounts, delays, and types are of the same size
        for (int i = 0; i < a.length(); i++) {
            amounts.add(new ArrayList<>());
            delays.add(new ArrayList<>());
            setDelays.add(new ArrayList<>());
            types.add(new ArrayList<>());

            for (int j = 0; j < a.getJSONArray(i).length(); j++) {
                amounts.get(i).add(a.getJSONArray(i).getInt(j));
                delays.get(i).add(d.getJSONArray(i).getDouble(j));
                setDelays.get(i).add(sd.getJSONArray(i).getDouble(j));
                types.get(i).add(Enemy.Type.valueOf(t.getJSONArray(i).getString(j)));
            }
        }
    }

    /**
     * helper function of {@link #next()} which increments spawned, set and wave accordingly
     */
    private void progressWave(){
        spawnedThisSet++;

        // if all enemies have been spawned in the set
        if(spawnedThisSet - amounts.get(curWave - 1).get(curSet) >= 0){
            spawnedThisSet = 0;
            curSet++;
            setStarted = false;
        }
        // if all sets in a wave have passed and there are more waves
        if(curSet - amounts.get(curWave - 1).size() >= 0 &&
                curWave < wavesToWin){
            Log.i("TowerDefense", "incrementing wave" + curWave + " -> " + (curWave + 1));
            curSet = 0;
            running = false;
        }
        // if user has won. all sets in last wave have passed
        else if(curSet - amounts.get(curWave - 1).size() >= 0 &&
                curWave >= wavesToWin){
            running = false;
            gameEnded = true;
        }
    }

    /**
     * @return current enemyType and progresses Wave
     */
    public Enemy.Type next(){
        Enemy.Type tempType = types.get(curWave - 1).get(curSet);
        progressWave();
        return tempType;
    }

    public void updateTimeSinceSpawn(double delta){
        timeSinceSpawn += delta;
    }

    public boolean delayPassed(){
        if(timeSinceSpawn >= delays.get(curWave - 1).get(curSet)){
            timeSinceSpawn = 0;
            return true;
        }
        return false;
    }

    public boolean setDelayPassed(){
        double setDelay = setDelays.get(curWave - 1).get(curSet);
        if(timeSinceSpawn >= setDelay){
            timeSinceSpawn -= setDelay;
            setStarted = true;
            return true;
        }
        return false;
    }

    public void nextWave(){
        curWave++;
    }

    public int getCurWave() {
        return curWave;
    }

    public boolean isWavesEnded() {
        return wavesEnded;
    }

    public boolean isRunning(){
        return running;
    }

    public void setRunning(boolean running){
        this.running = running;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    // overloaded functions used for testing
}
