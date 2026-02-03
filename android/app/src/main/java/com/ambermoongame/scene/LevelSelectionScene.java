package com.ambermoongame.scene;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.ambermoongame.input.TouchInputManager;

/**
 * Level selection scene for the Android port.
 * Allows players to select levels to play.
 */
public class LevelSelectionScene extends BaseScene {

    private String[] levelNames = {
        "Level 1: The Beginning",
        "Level 2: Forest Path",
        "Level 3: Cave Entrance",
        "Level 4: Underground",
        "Level 5: The Ruins"
    };

    private String[] levelPaths = {
        "levels/level_1.json",
        "levels/level_2.json",
        "levels/level_3.json",
        "levels/level_4.json",
        "levels/level_5.json"
    };

    private RectF[] levelButtons;
    private int selectedLevel = -1;

    private Paint titlePaint;
    private Paint buttonPaint;
    private Paint buttonSelectedPaint;
    private Paint textPaint;
    private Paint backButtonPaint;
    private RectF backButtonRect;

    public LevelSelectionScene() {
        super("LevelSelection");
    }

    @Override
    public void init() {
        super.init();

        int buttonWidth = 400;
        int buttonHeight = 60;
        int startY = 250;
        int spacing = 80;
        int centerX = AndroidSceneManager.SCREEN_WIDTH / 2;

        levelButtons = new RectF[levelNames.length];
        for (int i = 0; i < levelNames.length; i++) {
            int y = startY + i * spacing;
            levelButtons[i] = new RectF(
                centerX - buttonWidth / 2f, y,
                centerX + buttonWidth / 2f, y + buttonHeight
            );
        }

        // Back button
        backButtonRect = new RectF(50, 50, 200, 100);

        // Paints
        titlePaint = new Paint();
        titlePaint.setColor(Color.parseColor("#FFD700"));
        titlePaint.setTextSize(48);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setAntiAlias(true);

        buttonPaint = new Paint();
        buttonPaint.setColor(Color.parseColor("#2A2A4A"));

        buttonSelectedPaint = new Paint();
        buttonSelectedPaint.setColor(Color.parseColor("#4A4A8A"));

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        backButtonPaint = new Paint();
        backButtonPaint.setColor(Color.parseColor("#8A4A4A"));
    }

    @Override
    public void update(TouchInputManager input) {
        int touchX = input.getMouseX();
        int touchY = input.getMouseY();

        // Check hover
        selectedLevel = -1;
        for (int i = 0; i < levelButtons.length; i++) {
            if (levelButtons[i].contains(touchX, touchY)) {
                selectedLevel = i;
                break;
            }
        }

        // Handle click
        if (input.isScreenJustReleased()) {
            if (selectedLevel >= 0) {
                AndroidSceneManager.getInstance().loadLevel(levelPaths[selectedLevel]);
            } else if (backButtonRect.contains(touchX, touchY)) {
                AndroidSceneManager.getInstance().setScene("mainMenu", AndroidSceneManager.TRANSITION_FADE);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#1A1A2E"));

        // Title
        canvas.drawText("Select Level", AndroidSceneManager.SCREEN_WIDTH / 2f, 150, titlePaint);

        // Level buttons
        for (int i = 0; i < levelButtons.length; i++) {
            RectF rect = levelButtons[i];
            Paint paint = (i == selectedLevel) ? buttonSelectedPaint : buttonPaint;
            canvas.drawRoundRect(rect, 8, 8, paint);

            float textY = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2;
            canvas.drawText(levelNames[i], rect.centerX(), textY, textPaint);
        }

        // Back button
        canvas.drawRoundRect(backButtonRect, 8, 8, backButtonPaint);
        Paint backTextPaint = new Paint(textPaint);
        backTextPaint.setTextSize(20);
        canvas.drawText("< Back", backButtonRect.centerX(),
            backButtonRect.centerY() - (backTextPaint.descent() + backTextPaint.ascent()) / 2,
            backTextPaint);
    }

    @Override
    public boolean onBackPressed() {
        AndroidSceneManager.getInstance().setScene("mainMenu", AndroidSceneManager.TRANSITION_FADE);
        return true;
    }
}
