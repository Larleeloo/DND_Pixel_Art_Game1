package com.ambermoongame.core;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.ambermoongame.input.TouchInputManager;
import com.ambermoongame.input.AndroidControllerManager;
import com.ambermoongame.scene.AndroidSceneManager;
import com.ambermoongame.audio.AndroidAudioManager;
import com.ambermoongame.ui.TouchControlOverlay;

/**
 * Main game activity that hosts the game surface view and handles
 * the game loop, input routing, and lifecycle management.
 *
 * Equivalent to GameWindow.java + GamePanel.java from the desktop version.
 */
public class GameActivity extends Activity {

    private static final String TAG = "GameActivity";

    // Target resolution (matches desktop)
    public static final int TARGET_WIDTH = 1920;
    public static final int TARGET_HEIGHT = 1080;
    public static final int GROUND_Y = 720;

    // Game components
    private GameSurfaceView gameSurfaceView;
    private TouchControlOverlay touchControlOverlay;
    private FrameLayout rootLayout;

    // Input managers
    private TouchInputManager touchInputManager;
    private AndroidControllerManager controllerManager;

    // Scene manager
    private AndroidSceneManager sceneManager;

    // Vibration
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set fullscreen flags first (before setContentView)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize vibration
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Initialize input managers
        touchInputManager = new TouchInputManager(TARGET_WIDTH, TARGET_HEIGHT);
        controllerManager = new AndroidControllerManager();

        // Initialize scene manager
        sceneManager = AndroidSceneManager.getInstance();
        sceneManager.setContext(this);
        sceneManager.setInputManager(touchInputManager);
        sceneManager.setControllerManager(controllerManager);
        sceneManager.setAudioManager(AndroidAudioManager.getInstance());

        // Create the root layout
        rootLayout = new FrameLayout(this);

        // Create and add game surface view
        gameSurfaceView = new GameSurfaceView(this, sceneManager, touchInputManager, controllerManager);
        rootLayout.addView(gameSurfaceView);

        // Create and add touch control overlay
        touchControlOverlay = new TouchControlOverlay(this, touchInputManager);
        rootLayout.addView(touchControlOverlay);

        // Link touch overlay to input manager
        touchInputManager.setTouchControlOverlay(touchControlOverlay);

        setContentView(rootLayout);

        // Now set up immersive mode (after setContentView so DecorView exists)
        setupFullscreen();

        // Register all scenes
        registerScenes();

        // Start with main menu
        sceneManager.setScene("mainMenu");

        // Start background music
        AndroidAudioManager.getInstance().playMusic();
    }

    /**
     * Register all game scenes (mirrors desktop GamePanel).
     */
    private void registerScenes() {
        // Note: Scene implementations will use Android Canvas instead of Graphics2D
        // For initial port, we register placeholder scenes
        // Full scene implementations are in the scene/ package

        sceneManager.registerScene("mainMenu", new com.ambermoongame.scene.MainMenuScene());
        sceneManager.registerScene("levelSelection", new com.ambermoongame.scene.LevelSelectionScene());
        sceneManager.registerScene("spriteCustomization", new com.ambermoongame.scene.SpriteCharacterCustomization());
        sceneManager.registerScene("overworld", new com.ambermoongame.scene.OverworldScene());
        sceneManager.registerScene("creative", new com.ambermoongame.scene.CreativeScene());
        sceneManager.registerScene("lootGame", new com.ambermoongame.scene.LootGameScene());
    }

    /**
     * Sets up fullscreen immersive mode.
     */
    private void setupFullscreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    // ==================== Input Event Routing ====================

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Route touch events through input manager
        return touchInputManager.handleTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        // Handle controller analog stick input
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
            || (event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            return controllerManager.handleMotionEvent(event);
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle controller button presses
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            return controllerManager.handleKeyEvent(event) || super.onKeyDown(keyCode, event);
        }

        // Handle back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (sceneManager.isSettingsOpen()) {
                sceneManager.toggleSettings();
                return true;
            }
            // Let scene handle back navigation
            if (sceneManager.handleBackPressed()) {
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            return controllerManager.handleKeyEvent(event) || super.onKeyUp(keyCode, event);
        }
        return super.onKeyUp(keyCode, event);
    }

    // ==================== Vibration ====================

    /**
     * Trigger device vibration.
     * @param durationMs Duration in milliseconds
     * @param intensity Intensity from 0.0 to 1.0
     */
    public void vibrate(int durationMs, float intensity) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (!GamePreferences.isVibrationEnabled()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int amplitude = (int) (intensity * 255);
            amplitude = Math.max(1, Math.min(255, amplitude));
            VibrationEffect effect = VibrationEffect.createOneShot(durationMs, amplitude);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(durationMs);
        }
    }

    /**
     * Trigger a vibration pattern.
     * @param pattern Array of durations [delay, vibrate, delay, vibrate, ...]
     * @param repeat -1 for no repeat, 0+ for repeat from index
     */
    public void vibratePattern(long[] pattern, int repeat) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (!GamePreferences.isVibrationEnabled()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, repeat);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(pattern, repeat);
        }
    }

    /**
     * Stop any ongoing vibration.
     */
    public void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    // ==================== Lifecycle ====================

    @Override
    protected void onResume() {
        super.onResume();
        setupFullscreen();

        if (gameSurfaceView != null) {
            gameSurfaceView.resume();
        }

        AndroidAudioManager.getInstance().resumeMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (gameSurfaceView != null) {
            gameSurfaceView.pause();
        }

        AndroidAudioManager.getInstance().pauseMusic();

        // Auto-save on pause
        sceneManager.autoSave();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (gameSurfaceView != null) {
            gameSurfaceView.destroy();
        }

        AndroidAudioManager.getInstance().release();
        stopVibration();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setupFullscreen();
        }
    }

    // ==================== Accessors ====================

    public TouchInputManager getTouchInputManager() {
        return touchInputManager;
    }

    public AndroidControllerManager getControllerManager() {
        return controllerManager;
    }

    public AndroidSceneManager getSceneManager() {
        return sceneManager;
    }

    public GameSurfaceView getGameSurfaceView() {
        return gameSurfaceView;
    }

    public TouchControlOverlay getTouchControlOverlay() {
        return touchControlOverlay;
    }
}
