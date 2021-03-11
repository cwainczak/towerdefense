package com.wsu.towerdefense;

import android.content.Context;
import android.util.Log;
import com.wsu.towerdefense.map.MapReader;
import java.io.IOException;

public class Application extends android.app.Application {

    public static final boolean DEBUG = true;

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    /**
     * Run once per application
     */
    private void init() {
        context = getApplicationContext();

        // initialize maps
        try {
            MapReader.init(this);
        } catch (IOException e) {
            Log.e(getString(R.string.logcatKey), "Error while initializing maps", e);
        }
    }
}
