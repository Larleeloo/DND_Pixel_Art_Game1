package com.ambermoongame.scene;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.ambermoongame.audio.AndroidAudioManager;
import com.ambermoongame.core.GameActivity;
import com.ambermoongame.input.TouchInputManager;
import com.ambermoongame.input.AndroidControllerManager;
import com.ambermoongame.save.CloudSaveManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages Android game scenes and handles transitions between them.
 * Equivalent to SceneManager.java from the desktop version.
 *
 * Provides a centralized way to switch between different game states
 * with fade transition effects.
 */
public class AndroidSceneManager {

    private static final String TAG = "SceneManager";
    private static AndroidSceneManager instance;

    // Scene registry
    private Map<String, AndroidScene> scenes;
    private AndroidScene currentScene;
    private AndroidScene nextScene;

    // Transition state
    private boolean transitioning = false;
    private boolean fadeOutComplete = false;
    private float transitionAlpha = 0;
    private float transitionSpeed = 0.12f;

    // Transition types
    public static final int TRANSITION_NONE = 0;
    public static final int TRANSITION_FADE = 1;
    private int transitionType = TRANSITION_FADE;

    // Managers and context
    private Context context;
    private TouchInputManager inputManager;
    private AndroidControllerManager controllerManager;
    private AndroidAudioManager audioManager;

    // Settings overlay
    private boolean settingsOpen = false;

    // Transition paint
    private Paint transitionPaint;

    // Screen dimensions
    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;

    private AndroidSceneManager() {
        scenes = new HashMap<>();
        transitionPaint = new Paint();
        transitionPaint.setColor(Color.BLACK);
    }

    public static synchronized AndroidSceneManager getInstance() {
        if (instance == null) {
            instance = new AndroidSceneManager();
        }
        return instance;
    }

    // ==================== Initialization ====================

    public void setContext(Context context) {
        this.context = context;
    }

    public void setInputManager(TouchInputManager inputManager) {
        this.inputManager = inputManager;
    }

    public void setControllerManager(AndroidControllerManager controllerManager) {
        this.controllerManager = controllerManager;
    }

    public void setAudioManager(AndroidAudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public Context getContext() {
        return context;
    }

    public TouchInputManager getInputManager() {
        return inputManager;
    }

    public AndroidControllerManager getControllerManager() {
        return controllerManager;
    }

    public AndroidAudioManager getAudioManager() {
        return audioManager;
    }

    // ==================== Scene Registration ====================

    /**
     * Register a scene with a given name.
     * @param name Unique identifier for the scene
     * @param scene The scene instance
     */
    public void registerScene(String name, AndroidScene scene) {
        scenes.put(name, scene);
        Log.d(TAG, "Registered scene: " + name);
    }

    /**
     * Alias for registerScene (matches desktop API).
     */
    public void addScene(String name, AndroidScene scene) {
        registerScene(name, scene);
    }

    // ==================== Scene Switching ====================

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
        AndroidScene scene = scenes.get(name);
        if (scene == null) {
            Log.e(TAG, "Scene not found: " + name);
            return;
        }

        if (transition == TRANSITION_NONE) {
            // Immediate switch
            if (currentScene != null) {
                currentScene.dispose();
            }
            currentScene = scene;
            currentScene.init();
            Log.d(TAG, "Switched to scene: " + name);
        } else {
            // Prevent interrupting ongoing transition
            if (transitioning) {
                Log.d(TAG, "Transition in progress, ignoring switch to: " + name);
                return;
            }

            // Start transition
            nextScene = scene;
            transitioning = true;
            fadeOutComplete = false;
            transitionAlpha = 0;
            transitionType = transition;
            Log.d(TAG, "Starting transition to: " + name);
        }
    }

    /**
     * Load a level into a GameScene and switch to it.
     * @param levelPath Path to the level JSON file
     */
    public void loadLevel(String levelPath) {
        loadLevel(levelPath, TRANSITION_FADE);
    }

    public void loadLevel(String levelPath, int transition) {
        if (transitioning) {
            Log.d(TAG, "Transition in progress, ignoring level load: " + levelPath);
            return;
        }

        // Create a new GameScene for this level
        GameScene gameScene = new GameScene(levelPath);

        // Register with unique name
        String sceneName = "game_" + levelPath.hashCode();
        scenes.put(sceneName, gameScene);

        // Switch to it
        setScene(sceneName, transition);
    }

    // ==================== Update Loop ====================

    /**
     * Update the current scene.
     * @param input The touch input manager
     */
    public void update(TouchInputManager input) {
        // Reset click consumption
        if (input != null) {
            input.resetClickConsumed();
        }

        // Handle 'M' key/button for settings toggle
        if (input != null && input.isKeyJustPressed('m')) {
            toggleSettings();
            return;
        }

        // Check controller menu button
        if (controllerManager != null && controllerManager.isButtonStartJustPressed()) {
            toggleSettings();
            return;
        }

        // Handle transition
        if (transitioning) {
            updateTransition();
        }

        // Update scene if not in settings and not transitioning
        if (currentScene != null && !transitioning && !settingsOpen) {
            currentScene.update(input);
        }
    }

    private void updateTransition() {
        if (transitionType == TRANSITION_FADE) {
            if (!fadeOutComplete) {
                // Phase 1: Fade out
                transitionAlpha += transitionSpeed;
                if (transitionAlpha >= 1.0f) {
                    transitionAlpha = 1.0f;
                    fadeOutComplete = true;

                    // Switch scenes at peak fade
                    if (currentScene != null) {
                        currentScene.dispose();
                    }
                    currentScene = nextScene;
                    if (currentScene != null) {
                        currentScene.init();
                    }
                    nextScene = null;
                }
            } else {
                // Phase 2: Fade in
                transitionAlpha -= transitionSpeed;
                if (transitionAlpha <= 0) {
                    transitionAlpha = 0;
                    transitioning = false;
                    fadeOutComplete = false;
                    Log.d(TAG, "Transition complete: " + (currentScene != null ? currentScene.getName() : "null"));
                }
            }
        }
    }

    // ==================== Rendering ====================

    /**
     * Draw the current scene.
     * @param canvas The Android canvas
     */
    public void draw(Canvas canvas) {
        // Draw current scene
        if (currentScene != null) {
            currentScene.draw(canvas);
        }

        // Draw transition overlay
        if (transitioning && transitionType == TRANSITION_FADE) {
            int alpha = (int) (transitionAlpha * 255);
            transitionPaint.setAlpha(alpha);
            canvas.drawRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, transitionPaint);
        }

        // Draw settings overlay
        if (settingsOpen) {
            drawSettingsOverlay(canvas);
        }
    }

    private void drawSettingsOverlay(Canvas canvas) {
        // Semi-transparent background
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.argb(200, 0, 0, 0));
        canvas.drawRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, bgPaint);

        // Settings panel (simplified - full implementation would have proper UI)
        Paint panelPaint = new Paint();
        panelPaint.setColor(Color.argb(255, 42, 42, 74));
        canvas.drawRect(460, 140, 1460, 940, panelPaint);

        // Title
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(48);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Settings", SCREEN_WIDTH / 2f, 200, textPaint);

        // Close hint
        textPaint.setTextSize(24);
        canvas.drawText("Tap outside or press Back to close", SCREEN_WIDTH / 2f, 900, textPaint);
    }

    // ==================== Event Handling ====================

    public void onTouchPressed(int x, int y) {
        if (settingsOpen) {
            // Check if touch is outside settings panel
            if (x < 460 || x > 1460 || y < 140 || y > 940) {
                settingsOpen = false;
                return;
            }
            // Handle settings interaction
            return;
        }

        if (currentScene != null && !transitioning) {
            currentScene.onTouchPressed(x, y);
        }
    }

    public void onTouchReleased(int x, int y) {
        if (settingsOpen) return;

        if (currentScene != null && !transitioning) {
            currentScene.onTouchReleased(x, y);
        }
    }

    public void onTouchMoved(int x, int y) {
        if (settingsOpen) return;

        if (currentScene != null && !transitioning) {
            currentScene.onTouchMoved(x, y);
        }
    }

    /**
     * Handle back button press.
     * @return true if handled
     */
    public boolean handleBackPressed() {
        if (settingsOpen) {
            settingsOpen = false;
            return true;
        }

        if (currentScene != null) {
            return currentScene.onBackPressed();
        }

        return false;
    }

    // ==================== Settings ====================

    public void toggleSettings() {
        settingsOpen = !settingsOpen;
    }

    public boolean isSettingsOpen() {
        return settingsOpen;
    }

    // ==================== Save/Load ====================

    public void autoSave() {
        CloudSaveManager.getInstance().save();
    }

    // ==================== Accessors ====================

    public AndroidScene getCurrentScene() {
        return currentScene;
    }

    public AndroidScene getScene(String name) {
        return scenes.get(name);
    }

    public boolean isTransitioning() {
        return transitioning;
    }

    // ==================== Cleanup ====================

    public void dispose() {
        for (AndroidScene scene : scenes.values()) {
            scene.dispose();
        }
        scenes.clear();
        currentScene = null;
        nextScene = null;
    }
}
