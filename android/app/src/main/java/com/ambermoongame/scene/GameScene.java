package com.ambermoongame.scene;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.ambermoongame.graphics.AndroidAssetLoader;
import com.ambermoongame.input.TouchInputManager;

/**
 * Main gameplay scene for the Android port.
 * Loads and runs levels from JSON files.
 *
 * Equivalent to GameScene.java from desktop version.
 */
public class GameScene extends BaseScene {

    private String levelPath;
    private boolean levelLoaded = false;

    // Placeholder paints (full implementation would use entities)
    private Paint textPaint;
    private Paint groundPaint;
    private Paint playerPaint;

    // Player position (placeholder)
    private float playerX = 200;
    private float playerY = 600;
    private float playerVelX = 0;
    private float playerVelY = 0;
    private boolean onGround = false;

    // Game constants
    private static final float GRAVITY = 0.8f;
    private static final float MOVE_SPEED = 8.0f;
    private static final float JUMP_FORCE = -18.0f;
    private static final float GROUND_Y = 720;

    public GameScene(String levelPath) {
        super("GameScene");
        this.levelPath = levelPath;
    }

    @Override
    public void init() {
        super.init();

        // Initialize paints
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);
        textPaint.setAntiAlias(true);

        groundPaint = new Paint();
        groundPaint.setColor(Color.parseColor("#4A6A4A"));

        playerPaint = new Paint();
        playerPaint.setColor(Color.parseColor("#FFD700"));
        playerPaint.setAntiAlias(true);

        // Load level (placeholder)
        loadLevel();

        // Reset player position
        playerX = 200;
        playerY = GROUND_Y - 64;
    }

    private void loadLevel() {
        // TODO: Implement level loading from JSON
        // For now, just mark as loaded
        levelLoaded = true;
    }

    @Override
    public void update(TouchInputManager input) {
        if (!levelLoaded) return;

        // Handle movement
        playerVelX = 0;

        if (input.isKeyPressed('a')) {
            playerVelX = -MOVE_SPEED;
        }
        if (input.isKeyPressed('d')) {
            playerVelX = MOVE_SPEED;
        }

        // Handle jump
        if (input.isKeyJustPressed(' ') && onGround) {
            playerVelY = JUMP_FORCE;
            onGround = false;
        }

        // Apply gravity
        if (!onGround) {
            playerVelY += GRAVITY;
        }

        // Update position
        playerX += playerVelX;
        playerY += playerVelY;

        // Ground collision
        if (playerY >= GROUND_Y - 64) {
            playerY = GROUND_Y - 64;
            playerVelY = 0;
            onGround = true;
        }

        // Screen bounds
        if (playerX < 0) playerX = 0;
        if (playerX > AndroidSceneManager.SCREEN_WIDTH - 64) {
            playerX = AndroidSceneManager.SCREEN_WIDTH - 64;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        // Sky background
        canvas.drawColor(Color.parseColor("#87CEEB"));

        // Draw ground
        canvas.drawRect(0, GROUND_Y, AndroidSceneManager.SCREEN_WIDTH,
                       AndroidSceneManager.SCREEN_HEIGHT, groundPaint);

        // Draw player (placeholder rectangle)
        canvas.drawRect(playerX, playerY, playerX + 64, playerY + 64, playerPaint);

        // Draw debug info
        canvas.drawText("Level: " + levelPath, 20, 40, textPaint);
        canvas.drawText("Player: (" + (int)playerX + ", " + (int)playerY + ")", 20, 70, textPaint);
        canvas.drawText("Use D-Pad to move, Jump button to jump", 20, 100, textPaint);
    }

    @Override
    public boolean onBackPressed() {
        // Return to level selection
        AndroidSceneManager.getInstance().setScene("levelSelection", AndroidSceneManager.TRANSITION_FADE);
        return true;
    }
}
