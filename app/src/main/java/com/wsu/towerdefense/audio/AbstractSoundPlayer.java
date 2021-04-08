package com.wsu.towerdefense.audio;

import android.content.Context;

public abstract class AbstractSoundPlayer {

    protected final int sourceID;

    public AbstractSoundPlayer(int sourceID) {
        this.sourceID = sourceID;
    }

    public abstract void play(Context context, float volume);

    public abstract void release();

    protected float adjustVolume(float volume) {
        final float MAX_VOLUME = 100.0f;
        float adj = (float) (1 - (Math.log(MAX_VOLUME - volume) / Math.log(MAX_VOLUME)));
        if (adj > 2) {
            adj = 2;
        }
        return adj;
    }
}
