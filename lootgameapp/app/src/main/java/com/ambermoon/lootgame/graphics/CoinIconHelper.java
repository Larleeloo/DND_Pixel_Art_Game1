package com.ambermoon.lootgame.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.ReplacementSpan;
import android.util.TypedValue;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Replaces the Unicode coin character (\u25C8) in text with an inline
 * animated coin GIF sprite loaded from assets/icons/coin/coin.gif.
 */
public class CoinIconHelper {
    private static final String COIN_PATH = "icons/coin/coin.gif";
    private static final char COIN_CHAR = '\u25C8';
    private static final Map<TextView, Runnable> activeAnimations = new WeakHashMap<>();

    /**
     * Returns a CharSequence where every occurrence of \u25C8 is replaced
     * with a coin icon. Use this for Toasts and AlertDialog messages where
     * you can't call animate(). For TextViews, prefer setCoinText().
     */
    public static CharSequence withCoin(Context context, String text, float textSizeSp) {
        if (context == null || text == null) return text;

        int idx = text.indexOf(COIN_CHAR);
        if (idx == -1) return text;

        AssetLoader.ImageAsset asset = AssetLoader.load(COIN_PATH);
        if (asset == null || asset.bitmap == null) return text;

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp,
                context.getResources().getDisplayMetrics());
        int size = Math.round(px * 1.2f);

        SpannableString spannable = new SpannableString(text);
        while (idx != -1) {
            if (asset.isAnimated && asset.frames != null && asset.frames.size() > 1) {
                spannable.setSpan(new AnimatedCoinSpan(asset, size),
                        idx, idx + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                Bitmap scaled = Bitmap.createScaledBitmap(asset.bitmap, size, size, false);
                spannable.setSpan(new ImageSpan(context, scaled, ImageSpan.ALIGN_BASELINE),
                        idx, idx + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            idx = text.indexOf(COIN_CHAR, idx + 1);
        }

        return spannable;
    }

    /**
     * Sets coin-icon text on a TextView and starts animation if the GIF
     * is animated. Use this instead of tv.setText(withCoin(...)) for any
     * persistent TextView.
     */
    public static void setCoinText(TextView tv, String text, float textSizeSp) {
        tv.setText(withCoin(tv.getContext(), text, textSizeSp));
        animate(tv);
    }

    /**
     * Starts periodic invalidation on a TextView so that AnimatedCoinSpans
     * cycle through their frames. Safe to call multiple times on the same view.
     */
    public static void animate(final TextView tv) {
        if (tv == null) return;

        AssetLoader.ImageAsset asset = AssetLoader.load(COIN_PATH);
        if (asset == null || !asset.isAnimated) return;

        // Cancel any existing animation for this view
        Runnable old = activeAnimations.remove(tv);
        if (old != null) {
            tv.removeCallbacks(old);
        }

        final WeakReference<TextView> ref = new WeakReference<>(tv);
        Runnable tick = new Runnable() {
            @Override
            public void run() {
                TextView view = ref.get();
                if (view != null && view.isAttachedToWindow()) {
                    view.invalidate();
                    view.postDelayed(this, 50);
                } else {
                    if (view != null) activeAnimations.remove(view);
                }
            }
        };
        activeAnimations.put(tv, tick);
        tv.postDelayed(tick, 50);
    }

    /**
     * A ReplacementSpan that draws the current frame of the coin GIF animation.
     * Frames are pre-scaled to the target size to avoid allocation during draw().
     */
    static class AnimatedCoinSpan extends ReplacementSpan {
        private final Bitmap[] scaledFrames;
        private final int[] delays;
        private final int size;
        private final long startTime;
        private final int totalDuration;

        AnimatedCoinSpan(AssetLoader.ImageAsset asset, int size) {
            this.size = size;
            this.startTime = System.currentTimeMillis();

            scaledFrames = new Bitmap[asset.frames.size()];
            delays = new int[asset.delays.size()];
            int total = 0;
            for (int i = 0; i < asset.frames.size(); i++) {
                scaledFrames[i] = Bitmap.createScaledBitmap(
                        asset.frames.get(i), size, size, false);
                delays[i] = asset.delays.get(i);
                total += delays[i];
            }
            totalDuration = total > 0 ? total : 1;
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end,
                           Paint.FontMetricsInt fm) {
            if (fm != null) {
                fm.ascent = -size;
                fm.descent = 0;
                fm.top = fm.ascent;
                fm.bottom = fm.descent;
            }
            return size;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end,
                         float x, int top, int y, int bottom, Paint paint) {
            Bitmap frame = getCurrentFrame();
            if (frame == null) return;
            // Center vertically within the text line
            float cy = (top + bottom - size) / 2.0f;
            canvas.drawBitmap(frame, x, cy, null);
        }

        private Bitmap getCurrentFrame() {
            if (scaledFrames.length == 0) return null;
            if (scaledFrames.length == 1) return scaledFrames[0];

            long elapsed = System.currentTimeMillis() - startTime;
            long pos = elapsed % totalDuration;
            long accum = 0;
            for (int i = 0; i < scaledFrames.length; i++) {
                accum += delays[i];
                if (pos < accum) return scaledFrames[i];
            }
            return scaledFrames[0];
        }
    }
}
