package com.ambermoongame.scene;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.ambermoongame.input.TouchInputManager;

/**
 * Loot game mini-game scene for the Android port.
 * Placeholder implementation.
 */
public class LootGameScene extends BaseScene {

    private Paint titlePaint;
    private Paint textPaint;

    public LootGameScene() {
        super("LootGameScene");
    }

    @Override
    public void init() {
        super.init();

        titlePaint = new Paint();
        titlePaint.setColor(Color.parseColor("#FFD700"));
        titlePaint.setTextSize(48);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
    }

    @Override
    public void update(TouchInputManager input) {
        // Placeholder
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#4A2A4A"));

        canvas.drawText("Loot Game", AndroidSceneManager.SCREEN_WIDTH / 2f, 150, titlePaint);
        canvas.drawText("Collect items from chests!", AndroidSceneManager.SCREEN_WIDTH / 2f, 300, textPaint);
        canvas.drawText("Daily and Monthly chests with cooldowns", AndroidSceneManager.SCREEN_WIDTH / 2f, 350, textPaint);
    }
}
