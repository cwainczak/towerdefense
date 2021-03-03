package com.wsu.towerdefense;

import android.util.Log;
import com.wsu.towerdefense.map.MapReader;
import java.io.IOException;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }

    /**
     * Run once per application
     */
    private void init() {
        // initialize maps
        try {
            MapReader.init(this);
        } catch (IOException e) {
            Log.e(getString(R.string.logcatKey), "Error while initializing maps", e);
        }
    }
}
