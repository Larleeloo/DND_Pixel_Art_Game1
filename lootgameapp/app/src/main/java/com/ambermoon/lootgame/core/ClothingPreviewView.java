package com.ambermoon.lootgame.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.ambermoon.lootgame.graphics.AvatarRegistry;

/**
 * Custom View that displays the composited clothing preview sprite.
 * Renders a 64x64 sprite upscaled to the view size (typically 256x256)
 * using nearest-neighbor filtering for crisp pixel art.
 *
 * Supports two modes:
 * - Walking: Cycles through 15 frames at 100ms/frame (1.5s loop)
 * - Idle: Shows a single static frame
 */
public class ClothingPreviewView extends View {

    private Bitmap[] walkFrames;
    private Bitmap idleFrame;
    private boolean showWalking = true;
    private int currentFrame = 0;
    private boolean animating = false;

    private final Paint bitmapPaint = new Paint();
    private final Paint bgPaint = new Paint();
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    private final Runnable frameTickRunnable = new Runnable() {
        @Override
        public void run() {
            if (animating && showWalking && walkFrames != null && walkFrames.length > 0) {
                currentFrame = (currentFrame + 1) % walkFrames.length;
                invalidate();
                postDelayed(this, AvatarRegistry.FRAME_DELAY_MS);
            }
        }
    };

    public ClothingPreviewView(Context context) {
        super(context);
        bitmapPaint.setAntiAlias(false);
        bitmapPaint.setFilterBitmap(false); // nearest-neighbor for pixel art
        bgPaint.setColor(Color.parseColor("#12101A"));
        bgPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Sets the composited animation frames.
     *
     * @param walkFrames 15 composited walk frames (64x64 each)
     * @param idleFrame Single composited idle frame (64x64)
     */
    public void setFrames(Bitmap[] walkFrames, Bitmap idleFrame) {
        this.walkFrames = walkFrames;
        this.idleFrame = idleFrame;
        this.currentFrame = 0;
        invalidate();
        if (showWalking) {
            startAnimation();
        }
    }

    /**
     * Toggles between walking animation and idle display.
     */
    public void setShowWalking(boolean walking) {
        this.showWalking = walking;
        if (walking) {
            startAnimation();
        } else {
            stopAnimation();
        }
        invalidate();
    }

    public boolean isShowingWalking() {
        return showWalking;
    }

    private void startAnimation() {
        if (!animating) {
            animating = true;
            postDelayed(frameTickRunnable, AvatarRegistry.FRAME_DELAY_MS);
        }
    }

    private void stopAnimation() {
        animating = false;
        removeCallbacks(frameTickRunnable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (showWalking && walkFrames != null) {
            startAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();

        // Draw dark background
        canvas.drawRect(0, 0, w, h, bgPaint);

        // Get current frame to draw
        Bitmap frame = null;
        if (showWalking && walkFrames != null && walkFrames.length > 0) {
            frame = walkFrames[currentFrame % walkFrames.length];
        } else if (idleFrame != null) {
            frame = idleFrame;
        }

        if (frame == null) return;

        // Draw centered with nearest-neighbor upscaling
        int spriteW = frame.getWidth();
        int spriteH = frame.getHeight();

        // Calculate scale to fit view while maintaining aspect ratio
        int scale = Math.min(w / spriteW, h / spriteH);
        if (scale < 1) scale = 1;

        int scaledW = spriteW * scale;
        int scaledH = spriteH * scale;
        int offsetX = (w - scaledW) / 2;
        int offsetY = (h - scaledH) / 2;

        srcRect.set(0, 0, spriteW, spriteH);
        dstRect.set(offsetX, offsetY, offsetX + scaledW, offsetY + scaledH);
        canvas.drawBitmap(frame, srcRect, dstRect, bitmapPaint);
    }

    /**
     * Gets the current composited idle frame for use as a thumbnail.
     */
    public Bitmap getIdleFrame() {
        return idleFrame;
    }
}
