package com.ambermoongame.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Visual indicator for the ground level.
 * Should be added LAST to EntityManager so it draws on top.
 * Equivalent to entity/GroundEntity.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.Graphics/Graphics2D -> android.graphics.Canvas
 * - java.awt.Color               -> android.graphics.Color
 * - java.awt.BasicStroke          -> Paint.setStrokeWidth()
 * - java.awt.Rectangle           -> android.graphics.Rect
 */
public class GroundEntity extends Entity {

    private static final int GROUND_Y = 1800;

    private Paint linePaint;

    public GroundEntity() {
        super(0, GROUND_Y);

        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(5);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
    }

    @Override
    public Rect getBounds() {
        return new Rect(0, GROUND_Y, 1080, GROUND_Y + 10);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawLine(0, GROUND_Y, 1080, GROUND_Y, linePaint);
    }
}
