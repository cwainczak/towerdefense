package com.wsu.towerdefense.audio;

import android.content.Context;
import android.media.MediaPlayer;
import com.wsu.towerdefense.Util;
import java.util.ArrayList;
import java.util.List;

/**
 * Sound player that allows multiple instances of the same sound to be played at once. Internal
 * {@link MediaPlayer}s are automatically released when individual sounds are completed. All sounds
 * can be prematurely stopped with {@link #release()}.
 */
public class AdvancedSoundPlayer extends AbstractSoundPlayer {

    private final List<MediaPlayer> players;

    public AdvancedSoundPlayer(int sourceID) {
        super(sourceID);
        this.players = new ArrayList<>();
    }

    @Override
    public void play(Context context, float volume) {
        MediaPlayer player = MediaPlayer.create(context, sourceID);

        float v = Util.adjustVolume(volume);
        player.setVolume(v, v);

        player.setOnCompletionListener(mp -> {
            mp.release();
            players.remove(mp);
        });

        players.add(player);

        player.start();
    }

    @Override
    public void release() {
        for (MediaPlayer player : players) {
            player.release();
        }
        players.clear();
    }
}
