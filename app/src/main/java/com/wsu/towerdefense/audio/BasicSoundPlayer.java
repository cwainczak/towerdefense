package com.wsu.towerdefense.audio;

import android.content.Context;
import android.media.MediaPlayer;
import com.wsu.towerdefense.Util;
import java.io.IOException;

/**
 * Sound player that allows only one instance of the same sound to be played at once. If marked
 * <code>oneTime</code>, the internal {@link MediaPlayer} will automatically release itself.
 * Otherwise, {@link #release()} must be called to properly dispose of the media player.
 */
public class BasicSoundPlayer extends AbstractSoundPlayer {

    private MediaPlayer player;

    public BasicSoundPlayer(Context context, int sourceID, boolean oneTime) {
        super(sourceID);

        this.player = MediaPlayer.create(context, sourceID);
        if (oneTime) {
            this.player.setOnCompletionListener(mp -> {
                this.player.release();
                this.player = null;
            });
        }
    }

    @Override
    public void play(Context context, float volume) {
        if (this.player != null) {
            float v = Util.adjustVolume(volume);
            this.player.setVolume(v, v);

            this.player.stop();
            try {
                this.player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.player.start();
        }
    }

    public void stop() {
        this.player.stop();
    }

    @Override
    public void release() {
        if (this.player != null) {
            this.player.release();
            this.player = null;
        }
    }
}
