import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Manages all audio in the game - background music and sound effects
 */
public class AudioManager {

    private HashMap<String, Clip> soundEffects;
    private Clip backgroundMusic;
    private float musicVolume = 0.7f;
    private float sfxVolume = 0.8f;
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;

    public AudioManager() {
        soundEffects = new HashMap<>();
        System.out.println("AudioManager initialized");
    }

    /**
     * Load a sound effect
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
     * Load background music (loops)
     */
    public void loadMusic(String path) {
        try {
            File musicFile = new File(path);
            if (!musicFile.exists()) {
                System.out.println("Music file not found: " + path + " (will skip)");
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);

            setVolume(backgroundMusic, musicVolume);
            System.out.println("Loaded background music");
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Failed to load music: " + e.getMessage());
        }
    }

    /**
     * Play a sound effect by name (must be preloaded)
     */
    public void playSound(String name) {
        if (!sfxEnabled) return;

        // Check if it's a path (contains / or \) - if so, try to load/play dynamically
        if (name != null && (name.contains("/") || name.contains("\\"))) {
            playSoundFromPath(name);
            return;
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
        }
    }

    /**
     * Play a sound effect directly from a file path.
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

    /**
     * Start playing background music (loops infinitely)
     */
    public void playMusic() {
        if (backgroundMusic != null && musicEnabled && !backgroundMusic.isRunning()) {
            backgroundMusic.setFramePosition(0);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("Background music started");
        }
    }

    /**
     * Stop background music
     */
    public void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            System.out.println("Background music stopped");
        }
    }

    /**
     * Toggle music on/off
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
     * Toggle sound effects on/off
     */
    public void toggleSFX() {
        sfxEnabled = !sfxEnabled;
        System.out.println("Sound effects " + (sfxEnabled ? "enabled" : "disabled"));
    }

    /**
     * Set music volume (0.0 to 1.0)
     */
    public void setMusicVolume(float volume) {
        musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (backgroundMusic != null) {
            setVolume(backgroundMusic, musicVolume);
        }
    }

    /**
     * Set sound effects volume (0.0 to 1.0)
     */
    public void setSFXVolume(float volume) {
        sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    /**
     * Helper method to set volume on a clip
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
     * Clean up resources
     */
    public void dispose() {
        stopMusic();
        if (backgroundMusic != null) {
            backgroundMusic.close();
        }
        for (Clip clip : soundEffects.values()) {
            clip.close();
        }
        System.out.println("AudioManager disposed");
    }
}