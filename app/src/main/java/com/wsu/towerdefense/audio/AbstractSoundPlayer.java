package com.wsu.towerdefense.audio;

import android.content.Context;

public abstract class AbstractSoundPlayer {

    protected final int sourceID;

    public AbstractSoundPlayer(int sourceID) {
        this.sourceID = sourceID;
    }

    public abstract void play(Context context, float volume);

    public abstract void release();
}
