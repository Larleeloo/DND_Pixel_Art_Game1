package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.ambermoon.lootgame.graphics.AssetLoader;

/**
 * Custom View that plays an animated GIF sprite on loop.
 * Used in the Vault tab's detail panel to show item animation states
 * (idle, attack, block, critical, use, break).
 *
 * Top-level class to avoid D8 dex compiler crash on inner classes.
 */
public class AnimatedItemView extends View {
    private AssetLoader.ImageAsset asset;
    private int currentFrame = 0;
    private boolean playing = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable frameAdvancer;
    private final int fallbackColor;

    public AnimatedItemView(Context context, int fallbackColor) {
        super(context);
        this.fallbackColor = fallbackColor;
    }

    /** Load and start playing an animation from an asset path. */
    public void playAnimation(String assetPath) {
        stopAnimation();
        asset = AssetLoader.load(assetPath);
        currentFrame = 0;
        if (asset != null && asset.isAnimated && asset.frames != null && asset.frames.size() > 1) {
            playing = true;
            invalidate();
            scheduleNextFrame();
        } else {
            invalidate();
        }
    }

    /** Stop the current animation. */
    public void stopAnimation() {
        playing = false;
        if (frameAdvancer != null) {
            handler.removeCallbacks(frameAdvancer);
            frameAdvancer = null;
        }
    }

    private void scheduleNextFrame() {
        if (!playing || asset == null || asset.delays == null) return;
        int delay = asset.delays.get(currentFrame);
        frameAdvancer = () -> {
            if (!playing) return;
            currentFrame = (currentFrame + 1) % asset.frames.size();
            invalidate();
            scheduleNextFrame();
        };
        handler.postDelayed(frameAdvancer, Math.max(delay, 20));
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

        // Fallback: rarity-colored circle
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
