import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages game scenes and handles transitions between them.
 * Provides a centralized way to switch between different game states.
 */
class SceneManager {

    private static SceneManager instance;

    private Map<String, Scene> scenes;
    private Scene currentScene;
    private Scene nextScene;
    private boolean transitioning;
    private float transitionAlpha;
    private float transitionSpeed;
    private AudioManager audioManager;

    // Transition types
    public static final int TRANSITION_NONE = 0;
    public static final int TRANSITION_FADE = 1;
    private int transitionType;

    private SceneManager() {
        scenes = new HashMap<>();
        transitioning = false;
        transitionAlpha = 0;
        transitionSpeed = 0.05f;
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
    }

    /**
     * Get the audio manager.
     */
    public AudioManager getAudioManager() {
        return audioManager;
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
            // Start transition
            nextScene = scene;
            transitioning = true;
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
        if (transitioning) {
            updateTransition();
        }

        if (currentScene != null && !transitioning) {
            currentScene.update(input);
        }
    }

    /**
     * Update transition effect.
     */
    private void updateTransition() {
        if (transitionType == TRANSITION_FADE) {
            if (transitionAlpha < 1.0f && currentScene != null) {
                // Fade out current scene
                transitionAlpha += transitionSpeed;
                if (transitionAlpha >= 1.0f) {
                    transitionAlpha = 1.0f;
                    // Switch scenes at peak of fade
                    System.out.println("SceneManager: Transition at peak fade, switching scenes...");
                    if (currentScene != null) {
                        System.out.println("SceneManager: Disposing old scene: " + currentScene.getName());
                        currentScene.dispose();
                    }
                    currentScene = nextScene;
                    System.out.println("SceneManager: Initializing new scene: " + currentScene.getName());
                    currentScene.init();
                    nextScene = null;
                    System.out.println("SceneManager: Scene switch complete, starting fade in");
                }
            } else if (currentScene != null) {
                // Fade in new scene
                transitionAlpha -= transitionSpeed;
                if (transitionAlpha <= 0) {
                    transitionAlpha = 0;
                    transitioning = false;
                    System.out.println("SceneManager: Transition complete - now showing " + currentScene.getName());
                }
            } else {
                // No current scene, just init the new one
                System.out.println("SceneManager: No current scene, initializing new one directly");
                currentScene = nextScene;
                currentScene.init();
                nextScene = null;
                transitioning = false;
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
        if (currentScene != null && !transitioning) {
            currentScene.onMousePressed(x, y);
        }
    }

    public void onMouseReleased(int x, int y) {
        if (currentScene != null && !transitioning) {
            currentScene.onMouseReleased(x, y);
        }
    }

    public void onMouseDragged(int x, int y) {
        if (currentScene != null && !transitioning) {
            currentScene.onMouseDragged(x, y);
        }
    }

    public void onMouseMoved(int x, int y) {
        if (currentScene != null && !transitioning) {
            currentScene.onMouseMoved(x, y);
        }
    }

    public void onMouseClicked(int x, int y) {
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
