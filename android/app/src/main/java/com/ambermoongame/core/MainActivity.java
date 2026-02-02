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

        // Set fullscreen flags on window (works without DecorView)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            getWindow().setDecorFitsSystemWindows(false);
            // Note: InsetsController setup is done in GameActivity after setContentView
        } else {
            // Legacy fullscreen mode - use window flags instead of DecorView
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        // Fullscreen is handled via window flags, no need to call setupFullscreen again
    }
}
