package com.ambermoongame.scene;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.ambermoongame.input.TouchInputManager;

/**
 * Creative mode/level editor scene for the Android port.
 * Placeholder implementation.
 */
public class CreativeScene extends BaseScene {

    private Paint titlePaint;
    private Paint textPaint;

    public CreativeScene() {
        super("CreativeScene");
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
        canvas.drawColor(Color.parseColor("#2A2A4A"));

        canvas.drawText("Creative Mode", AndroidSceneManager.SCREEN_WIDTH / 2f, 150, titlePaint);
        canvas.drawText("Level editor - ported from desktop version", AndroidSceneManager.SCREEN_WIDTH / 2f, 300, textPaint);
        canvas.drawText("Use touch to place blocks, pinch to zoom", AndroidSceneManager.SCREEN_WIDTH / 2f, 350, textPaint);
    }
}
