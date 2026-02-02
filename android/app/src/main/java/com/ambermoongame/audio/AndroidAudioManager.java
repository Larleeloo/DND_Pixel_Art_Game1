package com.ambermoongame.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import com.ambermoongame.core.GamePreferences;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages audio playback for the Android port.
 * Uses MediaPlayer for background music and SoundPool for sound effects.
 *
 * Equivalent to AudioManager.java from desktop version.
 */
public class AndroidAudioManager {

    private static final String TAG = "AudioManager";
    private static final int MAX_STREAMS = 10;

    private static AndroidAudioManager instance;
    private static Context appContext;

    // Media player for background music
    private MediaPlayer musicPlayer;
    private String currentMusicPath;
    private boolean musicPaused = false;

    // Sound pool for sound effects
    private SoundPool soundPool;
    private Map<String, Integer> soundIds;
    private Map<String, Integer> streamIds;

    // Volume settings (0.0 to 1.0)
    private float musicVolume = 0.7f;
    private float sfxVolume = 0.8f;
    private boolean muted = false;

    private AndroidAudioManager() {
        soundIds = new HashMap<>();
        streamIds = new HashMap<>();
    }

    /**
     * Initialize audio manager with application context.
     */
    public static void initialize(Context context) {
        if (instance == null) {
            instance = new AndroidAudioManager();
        }
        appContext = context.getApplicationContext();
        instance.initSoundPool();
        instance.loadVolumesFromPrefs();
        Log.d(TAG, "AudioManager initialized");
    }

    public static AndroidAudioManager getInstance() {
        if (instance == null) {
            instance = new AndroidAudioManager();
        }
        return instance;
    }

    private void initSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

        soundPool = new SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(attributes)
            .build();
    }

    private void loadVolumesFromPrefs() {
        musicVolume = GamePreferences.getMusicVolume();
        sfxVolume = GamePreferences.getSfxVolume();
    }

    // ==================== Background Music ====================

    /**
     * Load and prepare background music from assets.
     * @param assetPath Path relative to assets folder
     */
    public void loadMusic(String assetPath) {
        if (appContext == null) return;

        try {
            // Release previous player
            if (musicPlayer != null) {
                musicPlayer.release();
            }

            musicPlayer = new MediaPlayer();
            AssetFileDescriptor afd = appContext.getAssets().openFd(assetPath);
            musicPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            musicPlayer.setLooping(true);
            musicPlayer.setVolume(muted ? 0 : musicVolume, muted ? 0 : musicVolume);
            musicPlayer.prepare();

            currentMusicPath = assetPath;
            Log.d(TAG, "Music loaded: " + assetPath);
        } catch (IOException e) {
            Log.w(TAG, "Failed to load music: " + assetPath + " - " + e.getMessage());
        }
    }

    /**
     * Load music from compressed sounds folder (MP3).
     */
    public void loadMusicMP3(String path) {
        loadMusic(path);
    }

    /**
     * Start playing background music.
     */
    public void playMusic() {
        if (musicPlayer != null && !musicPlayer.isPlaying()) {
            musicPlayer.start();
            musicPaused = false;
        }
    }

    /**
     * Pause background music.
     */
    public void pauseMusic() {
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.pause();
            musicPaused = true;
        }
    }

    /**
     * Resume background music.
     */
    public void resumeMusic() {
        if (musicPlayer != null && musicPaused) {
            musicPlayer.start();
            musicPaused = false;
        }
    }

    /**
     * Stop background music.
     */
    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            try {
                musicPlayer.prepare();
            } catch (IOException e) {
                Log.w(TAG, "Failed to prepare after stop");
            }
        }
    }

    /**
     * Check if music is currently playing.
     */
    public boolean isMusicPlaying() {
        return musicPlayer != null && musicPlayer.isPlaying();
    }

    // ==================== Sound Effects ====================

    /**
     * Load a sound effect from assets.
     * @param soundName Identifier for the sound
     * @param assetPath Path relative to assets folder
     */
    public void loadSound(String soundName, String assetPath) {
        if (appContext == null || soundPool == null) return;

        try {
            AssetFileDescriptor afd = appContext.getAssets().openFd(assetPath);
            int soundId = soundPool.load(afd, 1);
            soundIds.put(soundName, soundId);
            afd.close();
            Log.d(TAG, "Sound loaded: " + soundName);
        } catch (IOException e) {
            Log.w(TAG, "Failed to load sound: " + soundName + " - " + e.getMessage());
        }
    }

    /**
     * Play a loaded sound effect.
     * @param soundName Identifier of the sound to play
     */
    public void playSound(String soundName) {
        playSound(soundName, 1.0f);
    }

    /**
     * Play a loaded sound effect with custom volume.
     */
    public void playSound(String soundName, float volume) {
        if (muted || soundPool == null) return;

        Integer soundId = soundIds.get(soundName);
        if (soundId != null) {
            float actualVolume = sfxVolume * volume;
            int streamId = soundPool.play(soundId, actualVolume, actualVolume, 1, 0, 1.0f);
            streamIds.put(soundName, streamId);
        }
    }

    /**
     * Play a sound action (matches desktop SoundAction enum).
     */
    public void playAction(String action) {
        // Map action names to sound files
        // The sounds should be pre-loaded or loaded on demand
        playSound(action.toLowerCase());
    }

    /**
     * Stop a playing sound effect.
     */
    public void stopSound(String soundName) {
        Integer streamId = streamIds.get(soundName);
        if (streamId != null && soundPool != null) {
            soundPool.stop(streamId);
        }
    }

    // ==================== Volume Control ====================

    /**
     * Set music volume (0.0 to 1.0).
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
        if (musicPlayer != null) {
            float actualVolume = muted ? 0 : musicVolume;
            musicPlayer.setVolume(actualVolume, actualVolume);
        }
        GamePreferences.setMusicVolume(musicVolume);
    }

    /**
     * Get current music volume.
     */
    public float getMusicVolume() {
        return musicVolume;
    }

    /**
     * Set sound effects volume (0.0 to 1.0).
     */
    public void setSFXVolume(float volume) {
        this.sfxVolume = Math.max(0, Math.min(1, volume));
        GamePreferences.setSfxVolume(sfxVolume);
    }

    /**
     * Get current SFX volume.
     */
    public float getSFXVolume() {
        return sfxVolume;
    }

    /**
     * Mute/unmute all audio.
     */
    public void setMuteAll(boolean mute) {
        this.muted = mute;
        if (musicPlayer != null) {
            float volume = muted ? 0 : musicVolume;
            musicPlayer.setVolume(volume, volume);
        }
    }

    /**
     * Toggle mute state.
     */
    public void toggleMute() {
        setMuteAll(!muted);
    }

    /**
     * Check if audio is muted.
     */
    public boolean isMuted() {
        return muted;
    }

    // ==================== Lifecycle ====================

    /**
     * Release all audio resources.
     */
    public void release() {
        if (musicPlayer != null) {
            musicPlayer.release();
            musicPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        soundIds.clear();
        streamIds.clear();
        Log.d(TAG, "AudioManager released");
    }
}
