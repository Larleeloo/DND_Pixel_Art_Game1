package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.ambermoon.lootgame.graphics.AssetLoader;

/**
 * Custom View that renders a chest GIF animation.
 *
 * Supports two modes:
 * - Play-once mode: plays the GIF forward once and holds on the last frame
 *   (used when opening a chest). Call {@link #playOnce()} to trigger.
 * - Cooldown mode: shows the last frame of the GIF statically
 *   (used when chest is on cooldown). Call {@link #showLastFrame()}.
 * - Idle mode: shows the first frame statically
 *   (default when chest is available). Call {@link #showFirstFrame()}.
 *
 * Falls back to a colored rectangle if the GIF fails to load.
 *
 * Top-level class to avoid D8 dex compiler crash on inner classes.
 */
public class ChestIconView extends View {
    private AssetLoader.ImageAsset asset;
    private int currentFrame = 0;
    private boolean playing = false;
    private boolean holdLastFrame = false;
    private long frameStartTime = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable frameAdvancer;
    private final int fallbackColor;

    public ChestIconView(Context context, String assetPath, int fallbackColor) {
        super(context);
        this.fallbackColor = fallbackColor;
        this.asset = AssetLoader.load(assetPath);
    }

    /** Show the first frame (idle/available state). */
    public void showFirstFrame() {
        stopAnimation();
        currentFrame = 0;
        holdLastFrame = false;
        invalidate();
    }

    /** Show the last frame (cooldown state). */
    public void showLastFrame() {
        stopAnimation();
        if (asset != null && asset.frames != null && !asset.frames.isEmpty()) {
            currentFrame = asset.frames.size() - 1;
        }
        holdLastFrame = true;
        invalidate();
    }

    /** Play the GIF forward once, then hold on the last frame. */
    public void playOnce() {
        if (asset == null || asset.frames == null || asset.frames.size() <= 1) {
            return;
        }
        stopAnimation();
        currentFrame = 0;
        playing = true;
        holdLastFrame = false;
        invalidate();
        scheduleNextFrame();
    }

    private void scheduleNextFrame() {
        if (!playing || asset == null || asset.delays == null) return;
        int delay = asset.delays.get(currentFrame);
        frameAdvancer = () -> {
            if (!playing) return;
            currentFrame++;
            if (currentFrame >= asset.frames.size()) {
                // Reached end: hold last frame
                currentFrame = asset.frames.size() - 1;
                playing = false;
                holdLastFrame = true;
                invalidate();
                return;
            }
            invalidate();
            scheduleNextFrame();
        };
        handler.postDelayed(frameAdvancer, Math.max(delay, 20));
    }

    private void stopAnimation() {
        playing = false;
        if (frameAdvancer != null) {
            handler.removeCallbacks(frameAdvancer);
            frameAdvancer = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (asset != null && asset.frames != null && !asset.frames.isEmpty()) {
            int idx = Math.min(currentFrame, asset.frames.size() - 1);
            Bitmap frame = asset.frames.get(idx);
            if (frame != null && !frame.isRecycled()) {
                p.setFilterBitmap(false);
                canvas.drawBitmap(frame, null, new RectF(0, 0, getWidth(), getHeight()), p);
                return;
            }
        }

        // Fallback: colored rectangle
        p.setColor(fallbackColor);
        p.setStyle(Paint.Style.FILL);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 3f;
        canvas.drawCircle(cx, cy, radius, p);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}
