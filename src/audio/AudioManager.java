package audio;

import javax.sound.sampled.*;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all audio in the game - background music and sound effects.
 * Supports both WAV files (via javax.sound.sampled) and MP3 files (via JavaFX).
 *
 * MP3 Support:
 * - MP3 files should be placed in sounds/compressed/[category]/[action].mp3
 * - Use playAction(SoundAction) to play sounds by action type
 * - The system will automatically look for MP3 files first, then fall back to WAV
 *
 * Usage:
 *   AudioManager audio = new AudioManager();
 *   audio.playAction(SoundAction.JUMP);
 *   audio.playAction(SoundAction.USE_BATTLE_AXE);
 *   audio.playAction(SoundAction.OPEN_MONTHLY_CHEST);
 */
public class AudioManager {

    // WAV-based sound effects (legacy support)
    private HashMap<String, Clip> soundEffects;

    // MP3-based sound effects (JavaFX)
    private Map<String, MediaPlayer> mp3Cache;
    private Map<String, Media> mediaCache;

    // Background music
    private Clip backgroundMusicWav;
    private MediaPlayer backgroundMusicMp3;
    private boolean usingMp3Music = false;

    // Volume and state
    private float musicVolume = 0.7f;
    private float sfxVolume = 0.8f;
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;

    // JavaFX initialization flag
    private static boolean jfxInitialized = false;

    // Base paths for sound files
    private static final String WAV_BASE_PATH = "sounds";
    private static final String MP3_BASE_PATH = "sounds/compressed";

    // Maximum concurrent MediaPlayer instances per sound
    private static final int MAX_CONCURRENT_SOUNDS = 3;

    public AudioManager() {
        soundEffects = new HashMap<>();
        mp3Cache = new ConcurrentHashMap<>();
        mediaCache = new ConcurrentHashMap<>();

        // Initialize JavaFX toolkit
        initializeJavaFX();

        System.out.println("AudioManager initialized with MP3 support");
    }

    /**
     * Initialize JavaFX toolkit for MP3 playback.
     * This must be called before using any JavaFX media classes.
     */
    private void initializeJavaFX() {
        if (!jfxInitialized) {
            try {
                // Create a JFXPanel to initialize the JavaFX toolkit
                new JFXPanel();
                jfxInitialized = true;
                System.out.println("JavaFX toolkit initialized for MP3 support");
            } catch (Exception e) {
                System.out.println("Failed to initialize JavaFX: " + e.getMessage());
                System.out.println("MP3 playback will not be available");
            }
        }
    }

    // =========================================================================
    // ACTION-BASED SOUND PLAYBACK (Recommended API)
    // =========================================================================

    /**
     * Play a sound effect based on a game action.
     * This is the primary API for playing sounds in the game.
     *
     * The system will:
     * 1. First look for an MP3 file in sounds/compressed/[action_path].mp3
     * 2. If not found, fall back to WAV file in sounds/[action_path].wav
     * 3. If neither exists, fail silently (no error)
     *
     * @param action The game action to play a sound for
     */
    public void playAction(SoundAction action) {
        if (!sfxEnabled || action == null) return;

        String mp3Path = action.getMP3Path(MP3_BASE_PATH);
        String wavPath = action.getWAVPath(WAV_BASE_PATH);

        // Try MP3 first
        if (playMP3Sound(mp3Path)) {
            return;
        }

        // Fall back to WAV
        playSoundFromPath(wavPath);
    }

    /**
     * Play a sound effect by action name string.
     * Converts the string to a SoundAction enum value.
     *
     * @param actionName The action name (e.g., "JUMP", "USE_BATTLE_AXE")
     */
    public void playAction(String actionName) {
        if (actionName == null || actionName.isEmpty()) return;

        try {
            SoundAction action = SoundAction.valueOf(actionName.toUpperCase());
            playAction(action);
        } catch (IllegalArgumentException e) {
            // Not a valid SoundAction, try as a direct path/name
            playSound(actionName);
        }
    }

    // =========================================================================
    // MP3 PLAYBACK (JavaFX-based)
    // =========================================================================

    /**
     * Play an MP3 sound effect from a file path.
     *
     * @param path Path to the MP3 file
     * @return true if playback started successfully, false otherwise
     */
    public boolean playMP3Sound(String path) {
        if (!sfxEnabled || !jfxInitialized || path == null) return false;

        File soundFile = new File(path);
        if (!soundFile.exists()) {
            return false;
        }

        try {
            // Check if we have this media cached
            Media media = mediaCache.get(path);
            if (media == null) {
                media = new Media(soundFile.toURI().toString());
                mediaCache.put(path, media);
            }

            // Create a new MediaPlayer for this playback
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(sfxVolume);

            // Clean up when done
            player.setOnEndOfMedia(() -> {
                player.dispose();
            });

            player.setOnError(() -> {
                System.out.println("Error playing MP3: " + path);
                player.dispose();
            });

            player.play();
            return true;
        } catch (Exception e) {
            System.out.println("Failed to play MP3 " + path + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Load and play MP3 background music.
     *
     * @param path Path to the MP3 file
     */
    public void loadMusicMP3(String path) {
        if (!jfxInitialized) {
            System.out.println("JavaFX not initialized - cannot play MP3 music");
            return;
        }

        try {
            File musicFile = new File(path);
            if (!musicFile.exists()) {
                System.out.println("MP3 music file not found: " + path);
                return;
            }

            // Stop any existing music
            stopMusic();

            Media media = new Media(musicFile.toURI().toString());
            backgroundMusicMp3 = new MediaPlayer(media);
            backgroundMusicMp3.setVolume(musicVolume);
            backgroundMusicMp3.setCycleCount(MediaPlayer.INDEFINITE);
            usingMp3Music = true;

            System.out.println("Loaded MP3 background music: " + path);
        } catch (Exception e) {
            System.out.println("Failed to load MP3 music: " + e.getMessage());
            usingMp3Music = false;
        }
    }

    // =========================================================================
    // LEGACY WAV PLAYBACK (javax.sound.sampled)
    // =========================================================================

    /**
     * Load a WAV sound effect for later playback.
     */
    public void loadSound(String name, String path) {
        try {
            File soundFile = new File(path);
            if (!soundFile.exists()) {
                System.out.println("Sound file not found: " + path + " (will skip)");
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            soundEffects.put(name, clip);
            System.out.println("Loaded sound: " + name);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Failed to load sound " + name + ": " + e.getMessage());
        }
    }

    /**
     * Load WAV background music (loops).
     */
    public void loadMusic(String path) {
        try {
            File musicFile = new File(path);
            if (!musicFile.exists()) {
                System.out.println("Music file not found: " + path + " (will skip)");
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            backgroundMusicWav = AudioSystem.getClip();
            backgroundMusicWav.open(audioStream);

            setVolume(backgroundMusicWav, musicVolume);
            usingMp3Music = false;
            System.out.println("Loaded WAV background music");
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Failed to load music: " + e.getMessage());
        }
    }

    /**
     * Play a sound effect by name (must be preloaded) or by path.
     * This method maintains backward compatibility with existing code.
     */
    public void playSound(String name) {
        if (!sfxEnabled) return;

        // Check if it's a path (contains / or \) - if so, try to load/play dynamically
        if (name != null && (name.contains("/") || name.contains("\\"))) {
            // Try MP3 first if path ends with .mp3 or doesn't have extension
            if (name.endsWith(".mp3")) {
                if (playMP3Sound(name)) return;
            } else if (!name.contains(".")) {
                // No extension - try MP3 in compressed folder first
                if (playMP3Sound(MP3_BASE_PATH + "/" + name + ".mp3")) return;
                if (playMP3Sound(name + ".mp3")) return;
            }
            playSoundFromPath(name);
            return;
        }

        // Check if it's a SoundAction name
        try {
            SoundAction action = SoundAction.valueOf(name.toUpperCase());
            playAction(action);
            return;
        } catch (IllegalArgumentException e) {
            // Not a SoundAction, continue with legacy lookup
        }

        Clip clip = soundEffects.get(name);
        if (clip != null) {
            // Stop if already playing, reset to start
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            setVolume(clip, sfxVolume);
            clip.start();
        } else {
            // Try to find MP3 or WAV by name
            String mp3Path = MP3_BASE_PATH + "/" + name + ".mp3";
            String wavPath = WAV_BASE_PATH + "/" + name + ".wav";

            if (playMP3Sound(mp3Path)) return;
            playSoundFromPath(wavPath);
        }
    }

    /**
     * Play a WAV sound effect directly from a file path.
     * Caches the clip for reuse.
     */
    public void playSoundFromPath(String path) {
        if (!sfxEnabled || path == null) return;

        // Check if already loaded
        Clip clip = soundEffects.get(path);
        if (clip == null) {
            // Try to load it
            try {
                File soundFile = new File(path);
                if (!soundFile.exists()) {
                    // Sound file doesn't exist - fail silently
                    return;
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                clip = AudioSystem.getClip();
                clip.open(audioStream);
                soundEffects.put(path, clip);
                System.out.println("Loaded sound from path: " + path);
            } catch (Exception e) {
                // Failed to load - fail silently
                return;
            }
        }

        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            setVolume(clip, sfxVolume);
            clip.start();
        }
    }

    // =========================================================================
    // MUSIC CONTROL
    // =========================================================================

    /**
     * Start playing background music (loops infinitely).
     */
    public void playMusic() {
        if (!musicEnabled) return;

        if (usingMp3Music && backgroundMusicMp3 != null) {
            backgroundMusicMp3.seek(Duration.ZERO);
            backgroundMusicMp3.play();
            System.out.println("MP3 background music started");
        } else if (backgroundMusicWav != null && !backgroundMusicWav.isRunning()) {
            backgroundMusicWav.setFramePosition(0);
            backgroundMusicWav.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("WAV background music started");
        }
    }

    /**
     * Stop background music.
     */
    public void stopMusic() {
        if (usingMp3Music && backgroundMusicMp3 != null) {
            backgroundMusicMp3.stop();
            System.out.println("MP3 background music stopped");
        }
        if (backgroundMusicWav != null && backgroundMusicWav.isRunning()) {
            backgroundMusicWav.stop();
            System.out.println("WAV background music stopped");
        }
    }

    /**
     * Toggle music on/off.
     */
    public void toggleMusic() {
        musicEnabled = !musicEnabled;
        if (musicEnabled) {
            playMusic();
        } else {
            stopMusic();
        }
        System.out.println("Music " + (musicEnabled ? "enabled" : "disabled"));
    }

    /**
     * Toggle sound effects on/off.
     */
    public void toggleSFX() {
        sfxEnabled = !sfxEnabled;
        System.out.println("Sound effects " + (sfxEnabled ? "enabled" : "disabled"));
    }

    // =========================================================================
    // VOLUME CONTROL
    // =========================================================================

    /**
     * Set music volume (0.0 to 1.0).
     */
    public void setMusicVolume(float volume) {
        musicVolume = Math.max(0.0f, Math.min(1.0f, volume));

        if (usingMp3Music && backgroundMusicMp3 != null) {
            backgroundMusicMp3.setVolume(musicVolume);
        }
        if (backgroundMusicWav != null) {
            setVolume(backgroundMusicWav, musicVolume);
        }
    }

    /**
     * Set sound effects volume (0.0 to 1.0).
     */
    public void setSFXVolume(float volume) {
        sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    /**
     * Get the current music volume (0.0 to 1.0).
     */
    public float getMusicVolume() {
        return musicVolume;
    }

    /**
     * Get the current SFX volume (0.0 to 1.0).
     */
    public float getSFXVolume() {
        return sfxVolume;
    }

    // =========================================================================
    // STATE GETTERS AND SETTERS
    // =========================================================================

    /**
     * Check if music is enabled.
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    /**
     * Check if sound effects are enabled.
     */
    public boolean isSFXEnabled() {
        return sfxEnabled;
    }

    /**
     * Set music enabled state directly.
     */
    public void setMusicEnabled(boolean enabled) {
        if (musicEnabled != enabled) {
            musicEnabled = enabled;
            if (musicEnabled) {
                playMusic();
            } else {
                stopMusic();
            }
            System.out.println("Music " + (musicEnabled ? "enabled" : "disabled"));
        }
    }

    /**
     * Set SFX enabled state directly.
     */
    public void setSFXEnabled(boolean enabled) {
        sfxEnabled = enabled;
        System.out.println("Sound effects " + (sfxEnabled ? "enabled" : "disabled"));
    }

    /**
     * Mute or unmute all audio.
     */
    public void setMuteAll(boolean muted) {
        setMusicEnabled(!muted);
        setSFXEnabled(!muted);
    }

    /**
     * Check if all audio is muted.
     */
    public boolean isMuted() {
        return !musicEnabled && !sfxEnabled;
    }

    /**
     * Check if JavaFX is initialized and MP3 playback is available.
     */
    public boolean isMP3Available() {
        return jfxInitialized;
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Helper method to set volume on a WAV Clip.
     */
    private void setVolume(Clip clip, float volume) {
        if (clip != null) {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                // Convert 0.0-1.0 to decibels (logarithmic scale)
                float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
            } catch (Exception e) {
                // Volume control not supported
            }
        }
    }

    /**
     * Clean up resources.
     */
    public void dispose() {
        stopMusic();

        // Dispose WAV clips
        if (backgroundMusicWav != null) {
            backgroundMusicWav.close();
        }
        for (Clip clip : soundEffects.values()) {
            clip.close();
        }

        // Dispose MP3 players
        if (backgroundMusicMp3 != null) {
            backgroundMusicMp3.dispose();
        }
        for (MediaPlayer player : mp3Cache.values()) {
            player.dispose();
        }

        mp3Cache.clear();
        mediaCache.clear();
        soundEffects.clear();

        System.out.println("AudioManager disposed");
    }

    // =========================================================================
    // UTILITY METHODS FOR FOLDER STRUCTURE
    // =========================================================================

    /**
     * Get the base path for compressed MP3 sounds.
     */
    public static String getMP3BasePath() {
        return MP3_BASE_PATH;
    }

    /**
     * Get the base path for WAV sounds.
     */
    public static String getWAVBasePath() {
        return WAV_BASE_PATH;
    }

    /**
     * Get all sound action categories.
     */
    public static String[] getSoundCategories() {
        return new String[] {
            "player", "combat", "effects", "items", "tools", "blocks",
            "footsteps", "inventory", "chests", "doors", "mobs", "ui",
            "music", "ambient", "water", "events", "npc", "crafting", "special"
        };
    }
}
