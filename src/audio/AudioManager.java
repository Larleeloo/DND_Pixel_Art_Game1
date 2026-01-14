package audio;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages all audio in the game - background music and sound effects.
 * Supports both WAV files (via javax.sound.sampled) and MP3 files (via JLayer).
 *
 * SETUP REQUIRED FOR MP3 SUPPORT:
 * 1. Download JLayer from: https://github.com/umjammer/jlayer/releases
 *    Or Maven: https://mvnrepository.com/artifact/javazoom/jlayer/1.0.1
 * 2. Place jlayer-1.0.1.jar in the lib/ folder
 * 3. Add lib/jlayer-1.0.1.jar to your project's classpath
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

    // MP3 support via JLayer
    private static boolean jlayerAvailable = false;
    private ExecutorService audioExecutor;

    // Background music
    private Clip backgroundMusicWav;
    private volatile MP3Player backgroundMusicMp3;
    private volatile boolean musicLooping = false;
    private volatile String currentMusicPath = null;

    // Volume and state
    private float musicVolume = 0.7f;
    private float sfxVolume = 0.8f;
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;

    // Base paths for sound files
    private static final String WAV_BASE_PATH = "sounds";
    private static final String MP3_BASE_PATH = "sounds/compressed";

    // Static initialization to check for JLayer
    static {
        try {
            Class.forName("javazoom.jl.player.Player");
            jlayerAvailable = true;
            System.out.println("JLayer library found - MP3 support enabled");
        } catch (ClassNotFoundException e) {
            jlayerAvailable = false;
            System.out.println("JLayer library not found - MP3 support disabled");
            System.out.println("To enable MP3 support:");
            System.out.println("  1. Download jlayer-1.0.1.jar from https://mvnrepository.com/artifact/javazoom/jlayer/1.0.1");
            System.out.println("  2. Place it in the lib/ folder");
            System.out.println("  3. Add lib/jlayer-1.0.1.jar to your project classpath");
        }
    }

    public AudioManager() {
        soundEffects = new HashMap<>();
        audioExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "AudioManager-Worker");
            t.setDaemon(true);
            return t;
        });

        System.out.println("AudioManager initialized" + (jlayerAvailable ? " with MP3 support" : " (WAV only)"));
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

        // Try MP3 first (if JLayer is available)
        if (jlayerAvailable && playMP3Sound(mp3Path)) {
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
    // MP3 PLAYBACK (JLayer-based)
    // =========================================================================

    /**
     * Play an MP3 sound effect from a file path.
     * Playback is non-blocking (runs in background thread).
     *
     * @param path Path to the MP3 file
     * @return true if playback started successfully, false otherwise
     */
    public boolean playMP3Sound(String path) {
        if (!sfxEnabled || !jlayerAvailable || path == null) return false;

        File soundFile = new File(path);
        if (!soundFile.exists()) {
            return false;
        }

        // Play in background thread
        audioExecutor.submit(() -> {
            try {
                FileInputStream fis = new FileInputStream(soundFile);
                MP3Player player = new MP3Player(fis, sfxVolume);
                player.play();
            } catch (Exception e) {
                // Fail silently for missing/invalid files
            }
        });

        return true;
    }

    /**
     * Load and prepare MP3 background music for playback.
     *
     * @param path Path to the MP3 file
     */
    public void loadMusicMP3(String path) {
        if (!jlayerAvailable) {
            System.out.println("JLayer not available - cannot load MP3 music");
            System.out.println("Falling back to WAV if available");
            return;
        }

        File musicFile = new File(path);
        if (!musicFile.exists()) {
            System.out.println("MP3 music file not found: " + path);
            return;
        }

        // Stop any existing music
        stopMusic();

        currentMusicPath = path;
        System.out.println("Loaded MP3 background music: " + path);
    }

    /**
     * Start playing MP3 background music with looping.
     */
    private void playMusicMP3Loop() {
        if (!jlayerAvailable || currentMusicPath == null) return;

        audioExecutor.submit(() -> {
            while (musicLooping && musicEnabled) {
                try {
                    FileInputStream fis = new FileInputStream(currentMusicPath);
                    backgroundMusicMp3 = new MP3Player(fis, musicVolume);
                    backgroundMusicMp3.play();

                    // Small delay before looping
                    if (musicLooping && musicEnabled) {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.out.println("Error playing MP3 music: " + e.getMessage());
                    break;
                }
            }
        });
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

            // Stop any existing music
            stopMusic();

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            backgroundMusicWav = AudioSystem.getClip();
            backgroundMusicWav.open(audioStream);

            setVolume(backgroundMusicWav, musicVolume);
            currentMusicPath = null; // Clear MP3 path when loading WAV
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

            if (jlayerAvailable && playMP3Sound(mp3Path)) return;
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

        // Check if we have MP3 music loaded
        if (jlayerAvailable && currentMusicPath != null && currentMusicPath.endsWith(".mp3")) {
            musicLooping = true;
            playMusicMP3Loop();
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
        // Stop MP3 music
        musicLooping = false;
        if (backgroundMusicMp3 != null) {
            backgroundMusicMp3.stop();
            backgroundMusicMp3 = null;
        }

        // Stop WAV music
        if (backgroundMusicWav != null && backgroundMusicWav.isRunning()) {
            backgroundMusicWav.stop();
        }

        System.out.println("Background music stopped");
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

        if (backgroundMusicMp3 != null) {
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
     * Check if JLayer is available and MP3 playback is supported.
     */
    public boolean isMP3Available() {
        return jlayerAvailable;
    }

    /**
     * Check if JLayer library is loaded.
     */
    public static boolean isJLayerAvailable() {
        return jlayerAvailable;
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
                float dB = (float) (Math.log(Math.max(volume, 0.0001)) / Math.log(10.0) * 20.0);
                dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
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

        // Shutdown executor
        audioExecutor.shutdownNow();

        // Dispose WAV clips
        if (backgroundMusicWav != null) {
            backgroundMusicWav.close();
        }
        for (Clip clip : soundEffects.values()) {
            clip.close();
        }

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

    // =========================================================================
    // INNER CLASS: MP3Player (JLayer wrapper)
    // =========================================================================

    /**
     * Simple MP3 player wrapper using JLayer.
     * Handles playback in a way that allows stopping and volume control.
     */
    private static class MP3Player {
        private javazoom.jl.player.Player player;
        private InputStream inputStream;
        private volatile boolean stopped = false;
        private float volume;

        public MP3Player(InputStream inputStream, float volume) {
            this.inputStream = inputStream;
            this.volume = volume;
        }

        public void play() {
            try {
                // Create player with volume-adjusted audio device
                javazoom.jl.player.AudioDevice device = createAudioDevice();
                player = new javazoom.jl.player.Player(inputStream, device);

                if (!stopped) {
                    player.play();
                }
            } catch (Exception e) {
                // Playback error - fail silently
            } finally {
                close();
            }
        }

        public void stop() {
            stopped = true;
            if (player != null) {
                player.close();
            }
        }

        public void setVolume(float volume) {
            this.volume = volume;
            // Note: Volume changes take effect on next playback
        }

        private void close() {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                // Ignore
            }
        }

        /**
         * Create an audio device with volume control.
         */
        private javazoom.jl.player.AudioDevice createAudioDevice() {
            try {
                // Use FactoryRegistry to get default audio device
                return javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice();
            } catch (Exception e) {
                return null;
            }
        }
    }
}
