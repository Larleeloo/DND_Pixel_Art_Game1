package com.ambermoongame.scene;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.ambermoongame.input.TouchInputManager;

/**
 * Overworld/map scene for the Android port.
 * Placeholder implementation.
 */
public class OverworldScene extends BaseScene {

    private Paint titlePaint;
    private Paint textPaint;

    public OverworldScene() {
        super("OverworldScene");
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
        canvas.drawColor(Color.parseColor("#1A4A1A"));

        canvas.drawText("Overworld Map", AndroidSceneManager.SCREEN_WIDTH / 2f, 150, titlePaint);
        canvas.drawText("Coming soon - ported from desktop version", AndroidSceneManager.SCREEN_WIDTH / 2f, 300, textPaint);
    }
}
