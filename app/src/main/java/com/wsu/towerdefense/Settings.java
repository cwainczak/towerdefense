package com.wsu.towerdefense;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(
            context.getString(R.string.pref_file_key),
            Context.MODE_PRIVATE
        );
    }

    public static float getMusicVolume(Context context) {
        return getSharedPreferences(context)
            .getLong(
                context.getString(R.string.pref_key_music_volume),
                context.getResources().getInteger(R.integer.pref_def_volume)
            );
    }

    public static float getSFXVolume(Context context) {
        return getSharedPreferences(context)
            .getLong(
                context.getString(R.string.pref_key_sfx_volume),
                context.getResources().getInteger(R.integer.pref_def_volume)
            );
    }
}
