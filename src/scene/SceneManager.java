package scene;
import core.*;
import entity.*;
import entity.player.*;
import entity.mob.*;
import block.*;
import animation.*;
import graphics.*;
import level.*;
import audio.*;
import input.*;
import ui.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages game scenes and handles transitions between them.
 * Provides a centralized way to switch between different game states.
 */
public class SceneManager {

    private static SceneManager instance;

    private Map<String, Scene> scenes;
    private Scene currentScene;
    private Scene nextScene;
    private boolean transitioning;
    private boolean fadeOutComplete;  // Track whether we're in fade-out or fade-in phase
    private float transitionAlpha;
    private float transitionSpeed;
    private AudioManager audioManager;
    private InputManager inputManager;
    private SettingsOverlay settingsOverlay;

    // Transition types
    public static final int TRANSITION_NONE = 0;
    public static final int TRANSITION_FADE = 1;
    private int transitionType;

    private SceneManager() {
        scenes = new HashMap<>();
        transitioning = false;
        fadeOutComplete = false;
        transitionAlpha = 0;
        transitionSpeed = 0.12f;  // Faster transitions (was 0.05f)
        transitionType = TRANSITION_FADE;
    }

    /**
     * Get the singleton instance of SceneManager.
     */
    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    /**
     * Set the audio manager for scenes to use.
     */
    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
        // Create the settings overlay now that we have an audio manager
        this.settingsOverlay = new SettingsOverlay(audioManager);
    }

    /**
     * Get the audio manager.
     */
    public AudioManager getAudioManager() {
        return audioManager;
    }

    /**
     * Set the input manager for scenes to access.
     */
    public void setInputManager(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    /**
     * Get the input manager.
     */
    public InputManager getInputManager() {
        return inputManager;
    }

    /**
     * Get the settings overlay.
     */
    public SettingsOverlay getSettingsOverlay() {
        return settingsOverlay;
    }

    /**
     * Toggle the settings overlay visibility.
     */
    public void toggleSettings() {
        if (settingsOverlay != null) {
            settingsOverlay.toggle();
        }
    }

    /**
     * Check if the settings overlay is open.
     */
    public boolean isSettingsOpen() {
        return settingsOverlay != null && settingsOverlay.isVisible();
    }

    /**
     * Register a scene with a given name.
     * @param name Unique identifier for the scene
     * @param scene The scene instance
     */
    public void addScene(String name, Scene scene) {
        scenes.put(name, scene);
        System.out.println("SceneManager: Registered scene '" + name + "'");
    }

    /**
     * Switch to a scene immediately (no transition).
     * @param name The name of the scene to switch to
     */
    public void setScene(String name) {
        setScene(name, TRANSITION_NONE);
    }

    /**
     * Switch to a scene with a transition effect.
     * @param name The name of the scene to switch to
     * @param transition The transition type to use
     */
    public void setScene(String name, int transition) {
        Scene scene = scenes.get(name);
        if (scene == null) {
            System.err.println("SceneManager: Scene '" + name + "' not found!");
            return;
        }

        if (transition == TRANSITION_NONE) {
            // Immediate switch
            if (currentScene != null) {
                currentScene.dispose();
            }
            currentScene = scene;
            currentScene.init();
            System.out.println("SceneManager: Switched to scene '" + name + "'");
        } else {
            // Prevent interrupting an ongoing transition
            if (transitioning) {
                System.out.println("SceneManager: Transition already in progress, ignoring request to switch to '" + name + "'");
                return;
            }

            // Start transition
            nextScene = scene;
            transitioning = true;
            fadeOutComplete = false;
            transitionAlpha = 0;
            transitionType = transition;
            System.out.println("SceneManager: Starting transition to scene '" + name + "'");
        }
    }

    /**
     * Load a level into a GameScene and switch to it.
     * @param levelPath Path to the level JSON file
     */
    public void loadLevel(String levelPath) {
        loadLevel(levelPath, TRANSITION_FADE);
    }

    /**
     * Load a level into a GameScene and switch to it with transition.
     * @param levelPath Path to the level JSON file
     * @param transition The transition type to use
     */
    public void loadLevel(String levelPath, int transition) {
        System.out.println("SceneManager: loadLevel() called with path: " + levelPath);

        // Prevent interrupting an ongoing transition
        if (transitioning) {
            System.out.println("SceneManager: Transition already in progress, ignoring request to load level '" + levelPath + "'");
            return;
        }

        // Create a new GameScene for this level
        GameScene gameScene = new GameScene(levelPath);

        // Register it temporarily (or replace existing)
        String sceneName = "game_" + levelPath.hashCode();
        scenes.put(sceneName, gameScene);
        System.out.println("SceneManager: Registered scene '" + sceneName + "' for level " + levelPath);

        // Switch to it
        setScene(sceneName, transition);
    }

    /**
     * Update the current scene.
     * @param input The input manager
     */
    public void update(InputManager input) {
        // Reset UI click consumption flag at the start of each frame
        if (input != null) {
            input.resetClickConsumed();
        }

        // Update settings overlay (for controller rebinding detection)
        if (settingsOverlay != null) {
            settingsOverlay.update();
        }

        // Handle 'M' key for settings toggle (when not rebinding)
        if (input != null && input.isKeyJustPressed('m')) {
            if (settingsOverlay != null && !settingsOverlay.isVisible()) {
                settingsOverlay.show();
                return; // Don't process other input this frame
            }
        }

        if (transitioning) {
            updateTransition();
        }

        // Don't update scene if settings overlay is open
        if (currentScene != null && !transitioning && !isSettingsOpen()) {
            currentScene.update(input);
        }
    }

    /**
     * Handle key pressed events for settings overlay (for key rebinding).
     * @param keyCode The key code from KeyEvent
     * @return true if the event was consumed
     */
    public boolean handleKeyPressed(int keyCode) {
        if (settingsOverlay != null && settingsOverlay.isVisible()) {
            return settingsOverlay.handleKeyPressed(keyCode);
        }
        return false;
    }

    /**
     * Update transition effect.
     */
    private void updateTransition() {
        if (transitionType == TRANSITION_FADE) {
            if (!fadeOutComplete) {
                // Phase 1: Fade out current scene
                if (currentScene != null) {
                    transitionAlpha += transitionSpeed;
                    if (transitionAlpha >= 1.0f) {
                        transitionAlpha = 1.0f;
                        fadeOutComplete = true;
                        // Switch scenes at peak of fade
                        System.out.println("SceneManager: Transition at peak fade, switching scenes...");
                        System.out.println("SceneManager: Disposing old scene: " + currentScene.getName());
                        currentScene.dispose();
                        currentScene = nextScene;
                        if (currentScene != null) {
                            System.out.println("SceneManager: Initializing new scene: " + currentScene.getName());
                            currentScene.init();
                        } else {
                            System.err.println("SceneManager: ERROR - nextScene is null during transition!");
                            transitioning = false;
                            fadeOutComplete = false;
                            return;
                        }
                        nextScene = null;
                        System.out.println("SceneManager: Scene switch complete, starting fade in");
                    }
                } else {
                    // No current scene, just init the new one directly
                    if (nextScene != null) {
                        System.out.println("SceneManager: No current scene, initializing new one directly");
                        currentScene = nextScene;
                        currentScene.init();
                        nextScene = null;
                        fadeOutComplete = true;
                        transitionAlpha = 1.0f;
                    } else {
                        System.err.println("SceneManager: ERROR - No current scene and no next scene!");
                        transitioning = false;
                        fadeOutComplete = false;
                    }
                }
            } else {
                // Phase 2: Fade in new scene
                transitionAlpha -= transitionSpeed;
                if (transitionAlpha <= 0) {
                    transitionAlpha = 0;
                    transitioning = false;
                    fadeOutComplete = false;
                    System.out.println("SceneManager: Transition complete - now showing " + currentScene.getName());
                }
            }
        }
    }

    /**
     * Draw the current scene.
     * @param g The graphics context
     */
    public void draw(Graphics g) {
        if (currentScene != null) {
            currentScene.draw(g);
        }

        // Draw transition overlay
        if (transitioning && transitionType == TRANSITION_FADE) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0, 0, 0, (int)(transitionAlpha * 255)));
            g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        }

        // Draw settings overlay on top of everything
        if (settingsOverlay != null) {
            settingsOverlay.draw(g);
        }
    }

    /**
     * Get the current scene.
     */
    public Scene getCurrentScene() {
        return currentScene;
    }

    /**
     * Get a scene by name.
     */
    public Scene getScene(String name) {
        return scenes.get(name);
    }

    /**
     * Check if a transition is in progress.
     */
    public boolean isTransitioning() {
        return transitioning;
    }

    // Mouse event forwarding
    public void onMousePressed(int x, int y) {
        // Handle settings overlay first
        if (settingsOverlay != null && settingsOverlay.handleMousePressed(x, y)) {
            return; // Event consumed by settings
        }

        if (currentScene != null && !transitioning) {
            currentScene.onMousePressed(x, y);
        }
    }

    public void onMouseReleased(int x, int y) {
        // Handle settings overlay first
        if (settingsOverlay != null && settingsOverlay.isVisible()) {
            settingsOverlay.handleMouseReleased(x, y);
            return;
        }

        if (currentScene != null && !transitioning) {
            currentScene.onMouseReleased(x, y);
        }
    }

    public void onMouseDragged(int x, int y) {
        // Handle settings overlay first
        if (settingsOverlay != null && settingsOverlay.isVisible()) {
            settingsOverlay.handleMouseDragged(x, y);
            return;
        }

        if (currentScene != null && !transitioning) {
            currentScene.onMouseDragged(x, y);
        }
    }

    public void onMouseMoved(int x, int y) {
        // Handle settings overlay first
        if (settingsOverlay != null && settingsOverlay.isVisible()) {
            settingsOverlay.handleMouseMoved(x, y);
            return;
        }

        if (currentScene != null && !transitioning) {
            currentScene.onMouseMoved(x, y);
        }
    }

    public void onMouseClicked(int x, int y) {
        // Handle settings overlay first
        if (settingsOverlay != null && settingsOverlay.handleMouseClicked(x, y)) {
            return; // Event consumed by settings
        }

        if (currentScene != null && !transitioning) {
            currentScene.onMouseClicked(x, y);
        }
    }

    /**
     * Dispose all scenes and clean up.
     */
    public void dispose() {
        for (Scene scene : scenes.values()) {
            scene.dispose();
        }
        scenes.clear();
        currentScene = null;
        nextScene = null;
    }
}
