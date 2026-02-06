package com.ambermoon.lootgame.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import com.ambermoon.lootgame.core.GamePreferences;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private SoundPool soundPool;
    private Map<String, Integer> sounds = new HashMap<>();
    private boolean loaded = false;

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    public void init(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(attrs)
                .build();
        loaded = true;
    }

    public void play(String name) {
        if (!loaded || soundPool == null) return;
        Integer id = sounds.get(name);
        if (id != null) {
            float vol = GamePreferences.getSfxVolume();
            soundPool.play(id, vol, vol, 1, 0, 1.0f);
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        loaded = false;
    }
}
