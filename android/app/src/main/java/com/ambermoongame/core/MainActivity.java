package com.ambermoongame.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import com.ambermoongame.save.CloudSaveManager;
import com.ambermoongame.audio.AndroidAudioManager;

/**
 * Main entry point for The Amber Moon Android port.
 * Handles app initialization and launches the game activity.
 *
 * This activity serves as the launcher and handles initial setup,
 * then immediately transitions to GameActivity for the main game loop.
 */
public class MainActivity extends Activity {

    private static final String TAG = "AmberMoon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up fullscreen immersive mode
        setupFullscreen();

        // Initialize global managers
        initializeManagers();

        // Launch game activity
        launchGame();
    }

    /**
     * Sets up fullscreen immersive mode to hide system UI.
     */
    private void setupFullscreen() {
        // Keep screen on during gameplay
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // Legacy fullscreen mode
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    /**
     * Initialize global singleton managers.
     */
    private void initializeManagers() {
        // Initialize cloud save manager with app context
        CloudSaveManager.initialize(getApplicationContext());

        // Initialize audio manager
        AndroidAudioManager.initialize(getApplicationContext());

        // Load saved preferences
        GamePreferences.initialize(getApplicationContext());
    }

    /**
     * Launch the main game activity.
     */
    private void launchGame() {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);

        // Finish this activity so back button exits app
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupFullscreen();
    }
}
