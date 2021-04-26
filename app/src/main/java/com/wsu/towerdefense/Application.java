package com.wsu.towerdefense;

import android.content.Context;
import android.util.Log;
import com.wsu.towerdefense.Model.tower.UpgradeReader;

import java.io.IOException;

public class Application extends android.app.Application {

    public static final boolean DEBUG = false;

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

        // initialize upgrades
        try {
            UpgradeReader.init(this);
        } catch (IOException e) {
            Log.e(getString(R.string.logcatKey), "Error while initializing upgrades", e);
        }
    }
}
