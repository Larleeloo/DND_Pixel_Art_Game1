package com.ambermoongame.scene;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.ambermoongame.input.TouchInputManager;

/**
 * Main menu scene for the Android port.
 * Equivalent to MainMenuScene.java from desktop version.
 */
public class MainMenuScene extends BaseScene {

    // Button definitions
    private static final int BUTTON_WIDTH = 300;
    private static final int BUTTON_HEIGHT = 60;
    private static final int BUTTON_SPACING = 20;
    private static final int BUTTON_START_Y = 400;

    private String[] buttonLabels = {
        "Play",
        "Creative Mode",
        "Character",
        "Loot Game",
        "Settings",
        "Exit"
    };

    private RectF[] buttonRects;
    private int hoveredButton = -1;
    private int pressedButton = -1;

    // Paints
    private Paint titlePaint;
    private Paint buttonPaint;
    private Paint buttonHoverPaint;
    private Paint buttonTextPaint;
    private Paint subtitlePaint;

    public MainMenuScene() {
        super("MainMenu");
    }

    @Override
    public void init() {
        super.init();

        // Calculate button positions (centered)
        int centerX = AndroidSceneManager.SCREEN_WIDTH / 2;
        buttonRects = new RectF[buttonLabels.length];

        for (int i = 0; i < buttonLabels.length; i++) {
            int y = BUTTON_START_Y + i * (BUTTON_HEIGHT + BUTTON_SPACING);
            buttonRects[i] = new RectF(
                centerX - BUTTON_WIDTH / 2f,
                y,
                centerX + BUTTON_WIDTH / 2f,
                y + BUTTON_HEIGHT
            );
        }

        // Initialize paints
        titlePaint = new Paint();
        titlePaint.setColor(Color.parseColor("#FFD700")); // Gold
        titlePaint.setTextSize(72);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setAntiAlias(true);

        subtitlePaint = new Paint();
        subtitlePaint.setColor(Color.parseColor("#AAAACC"));
        subtitlePaint.setTextSize(24);
        subtitlePaint.setTextAlign(Paint.Align.CENTER);
        subtitlePaint.setAntiAlias(true);

        buttonPaint = new Paint();
        buttonPaint.setColor(Color.parseColor("#2A2A4A"));
        buttonPaint.setAntiAlias(true);

        buttonHoverPaint = new Paint();
        buttonHoverPaint.setColor(Color.parseColor("#4A4A6A"));
        buttonHoverPaint.setAntiAlias(true);

        buttonTextPaint = new Paint();
        buttonTextPaint.setColor(Color.WHITE);
        buttonTextPaint.setTextSize(28);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
        buttonTextPaint.setAntiAlias(true);
    }

    @Override
    public void update(TouchInputManager input) {
        // Check for button hover (touch move)
        int touchX = input.getMouseX();
        int touchY = input.getMouseY();

        hoveredButton = -1;
        for (int i = 0; i < buttonRects.length; i++) {
            if (buttonRects[i].contains(touchX, touchY)) {
                hoveredButton = i;
                break;
            }
        }

        // Handle button press
        if (input.isScreenJustTouched() && hoveredButton >= 0) {
            pressedButton = hoveredButton;
        }

        // Handle button release
        if (input.isScreenJustReleased() && pressedButton >= 0) {
            if (pressedButton == hoveredButton) {
                handleButtonClick(pressedButton);
            }
            pressedButton = -1;
        }
    }

    private void handleButtonClick(int index) {
        AndroidSceneManager sceneManager = AndroidSceneManager.getInstance();

        switch (index) {
            case 0: // Play
                sceneManager.setScene("levelSelection", AndroidSceneManager.TRANSITION_FADE);
                break;
            case 1: // Creative Mode
                sceneManager.setScene("creative", AndroidSceneManager.TRANSITION_FADE);
                break;
            case 2: // Character
                sceneManager.setScene("spriteCustomization", AndroidSceneManager.TRANSITION_FADE);
                break;
            case 3: // Loot Game
                sceneManager.setScene("lootGame", AndroidSceneManager.TRANSITION_FADE);
                break;
            case 4: // Settings
                sceneManager.toggleSettings();
                break;
            case 5: // Exit
                // On Android, use back button behavior instead
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        // Background
        canvas.drawColor(Color.parseColor("#1A1A2E"));

        // Title
        canvas.drawText("The Amber Moon", AndroidSceneManager.SCREEN_WIDTH / 2f, 200, titlePaint);

        // Subtitle
        canvas.drawText("A 2D Platformer Adventure", AndroidSceneManager.SCREEN_WIDTH / 2f, 260, subtitlePaint);

        // Draw buttons
        for (int i = 0; i < buttonRects.length; i++) {
            RectF rect = buttonRects[i];

            // Button background
            Paint paint = (i == hoveredButton || i == pressedButton) ? buttonHoverPaint : buttonPaint;
            canvas.drawRoundRect(rect, 8, 8, paint);

            // Button border
            Paint borderPaint = new Paint();
            borderPaint.setColor(Color.parseColor("#4A4A6A"));
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(2);
            borderPaint.setAntiAlias(true);
            canvas.drawRoundRect(rect, 8, 8, borderPaint);

            // Button text
            float textY = rect.centerY() - (buttonTextPaint.descent() + buttonTextPaint.ascent()) / 2;
            canvas.drawText(buttonLabels[i], rect.centerX(), textY, buttonTextPaint);
        }

        // Version info
        Paint versionPaint = new Paint();
        versionPaint.setColor(Color.parseColor("#666688"));
        versionPaint.setTextSize(18);
        versionPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Android Port v1.0", AndroidSceneManager.SCREEN_WIDTH - 20, AndroidSceneManager.SCREEN_HEIGHT - 20, versionPaint);
    }

    @Override
    public void onTouchPressed(int x, int y) {
        for (int i = 0; i < buttonRects.length; i++) {
            if (buttonRects[i].contains(x, y)) {
                pressedButton = i;
                break;
            }
        }
    }

    @Override
    public void onTouchReleased(int x, int y) {
        if (pressedButton >= 0 && buttonRects[pressedButton].contains(x, y)) {
            handleButtonClick(pressedButton);
        }
        pressedButton = -1;
    }

    @Override
    public boolean onBackPressed() {
        // Exit app from main menu
        return false; // Let system handle it
    }
}
