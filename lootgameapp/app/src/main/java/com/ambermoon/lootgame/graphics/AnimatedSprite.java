package com.ambermoon.lootgame.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.List;

/**
 * Wraps a decoded GIF for easy animated rendering.
 */
public class AnimatedSprite {
    private List<Bitmap> frames;
    private List<Integer> delays;
    private int currentFrame;
    private long elapsed;
    private boolean looping = true;
    private boolean paused = false;
    private Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

    public AnimatedSprite(List<Bitmap> frames, List<Integer> delays) {
        this.frames = frames;
        this.delays = delays;
        this.currentFrame = 0;
        this.elapsed = 0;
    }

    public AnimatedSprite(GifDecoder.GifResult gif) {
        this(gif.frames, gif.delays);
    }

    public void update(long deltaMs) {
        if (paused || frames == null || frames.size() <= 1) return;
        elapsed += deltaMs;
        while (elapsed >= delays.get(currentFrame)) {
            elapsed -= delays.get(currentFrame);
            currentFrame++;
            if (currentFrame >= frames.size()) {
                if (looping) {
                    currentFrame = 0;
                } else {
                    currentFrame = frames.size() - 1;
                    paused = true;
                    return;
                }
            }
        }
    }

    public void draw(Canvas canvas, float x, float y, float w, float h) {
        if (frames == null || frames.isEmpty()) return;
        Bitmap frame = frames.get(currentFrame);
        canvas.drawBitmap(frame, null, new RectF(x, y, x + w, y + h), paint);
    }

    public Bitmap getCurrentFrame() {
        if (frames == null || frames.isEmpty()) return null;
        return frames.get(currentFrame);
    }

    public void reset() { currentFrame = 0; elapsed = 0; paused = false; }
    public void setLooping(boolean loop) { this.looping = loop; }
    public void setPaused(boolean paused) { this.paused = paused; }
    public boolean isAnimated() { return frames != null && frames.size() > 1; }
    public int getFrameCount() { return frames != null ? frames.size() : 0; }
    public int getWidth() { return frames != null && !frames.isEmpty() ? frames.get(0).getWidth() : 0; }
    public int getHeight() { return frames != null && !frames.isEmpty() ? frames.get(0).getHeight() : 0; }
}
