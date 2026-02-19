package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.*;
import android.view.View;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ItemRegistry;
import com.ambermoon.lootgame.graphics.AssetLoader;

/**
 * Custom View that renders a single slot machine reel with realistic vertical
 * scrolling animation. Symbols scroll downward through the visible window and
 * decelerate smoothly before landing on the target symbol, mimicking a real
 * slot machine.
 *
 * The reel displays a vertical strip of symbols. During a spin, the strip
 * scrolls rapidly, then decelerates over a configurable duration, finally
 * snapping to center the target symbol in the view.
 *
 * Slot symbols map to item IDs:
 *   Apple  -> "apple"
 *   Sword  -> "katana"
 *   Shield -> "steel_shield"
 *   Gem    -> "diamond"
 *   Star   -> "magic_gemstone"
 *   Crown  -> "crown"
 *
 * Top-level class to avoid D8 dex compiler crash on inner classes.
 */
public class SlotReelView extends View {
    /** Item IDs corresponding to each symbol index. */
    public static final String[] SYMBOL_ITEM_IDS = {
        "apple", "katana", "steel_shield", "diamond", "magic_gemstone", "crown"
    };

    /** Rarity colors per symbol for fallback circles. */
    public static final int[] SYMBOL_COLORS = {
        Color.WHITE,                    // Common (Apple)
        Color.rgb(30, 255, 30),         // Uncommon (Sword)
        Color.rgb(30, 100, 255),        // Rare (Shield)
        Color.rgb(180, 30, 255),        // Epic (Gem)
        Color.rgb(255, 165, 0),         // Legendary (Star)
        Color.rgb(0, 255, 255)          // Mythic (Crown)
    };

    private static final int SYMBOL_COUNT = SYMBOL_ITEM_IDS.length;

    /** How many full symbol cycles to scroll through during spin. */
    private static final int SPIN_CYCLES = 4;

    private int symbolIndex = -1;  // -1 means "?" state
    private Bitmap[] cachedIcons;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // --- Spin animation state ---
    private boolean spinning = false;
    private int targetSymbol = -1;
    private long spinStartTime;
    private long spinDuration;
    private float totalScrollDistance;
    private Runnable onStopCallback;

    public SlotReelView(Context context) {
        super(context);
        setBackgroundColor(Color.parseColor("#28233A"));
        preloadIcons();
    }

    private void preloadIcons() {
        cachedIcons = new Bitmap[SYMBOL_ITEM_IDS.length];
        for (int i = 0; i < SYMBOL_ITEM_IDS.length; i++) {
            Item template = ItemRegistry.getTemplate(SYMBOL_ITEM_IDS[i]);
            if (template != null && template.getIcon() != null && !template.getIcon().isRecycled()) {
                cachedIcons[i] = template.getIcon();
            } else {
                // Try loading directly from assets
                String path = "items/" + SYMBOL_ITEM_IDS[i] + "/idle.gif";
                AssetLoader.ImageAsset asset = AssetLoader.load(path);
                if (asset != null && asset.bitmap != null) {
                    cachedIcons[i] = asset.bitmap;
                }
            }
        }
    }

    /** Set the displayed symbol (0-5), or -1 for "?" state. No animation. */
    public void setSymbol(int symbolIndex) {
        this.symbolIndex = symbolIndex;
        this.spinning = false;
        this.targetSymbol = -1;
        invalidate();
    }

    /**
     * Start a spinning animation that lands on the given target symbol.
     *
     * @param target     the symbol index (0-5) to land on
     * @param durationMs how long the reel should spin before stopping
     * @param onStop     callback invoked when the reel finishes stopping
     */
    public void spin(int target, long durationMs, Runnable onStop) {
        this.spinning = true;
        this.targetSymbol = target;
        this.spinDuration = durationMs;
        this.onStopCallback = onStop;
        this.spinStartTime = System.currentTimeMillis();

        // Total distance = enough full cycles plus distance to land on target.
        // Each symbol occupies one "cell height" worth of scroll distance.
        // We scroll through SPIN_CYCLES full sets of symbols, plus enough extra
        // to end centered on the target symbol.
        float cellHeight = getHeight();
        this.totalScrollDistance = (SPIN_CYCLES * SYMBOL_COUNT + target) * cellHeight;

        // Kick off the animation loop
        postInvalidateOnAnimation();
    }

    /**
     * Easing function: decelerate (ease-out cubic).
     * Input t in [0,1], output in [0,1].
     */
    private float easeOutCubic(float t) {
        float f = 1f - t;
        return 1f - f * f * f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (spinning) {
            drawSpinning(canvas);
            return;
        }

        if (symbolIndex < 0 || symbolIndex >= SYMBOL_ITEM_IDS.length) {
            // Draw "?" placeholder
            paint.setColor(Color.parseColor("#666688"));
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(48);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("?", getWidth() / 2f, getHeight() / 2f + 16, paint);
            return;
        }

        drawSymbol(canvas, symbolIndex, 0f);
    }

    /**
     * Draw the spinning reel. Calculates current scroll offset based on elapsed
     * time and easing, then draws the visible symbols at their offset positions.
     */
    private void drawSpinning(Canvas canvas) {
        long elapsed = System.currentTimeMillis() - spinStartTime;
        float t = Math.min(1f, (float) elapsed / spinDuration);
        float eased = easeOutCubic(t);

        float cellH = getHeight();
        float scrollOffset = eased * totalScrollDistance;

        // Which symbol is at the center? The strip is an infinite repeating
        // sequence of symbols [0,1,2,3,4,5,0,1,2,...]. scrollOffset tells us
        // how far we've scrolled in pixels. Convert to symbol-space:
        float symbolPos = scrollOffset / cellH;
        int centerIdx = ((int) symbolPos) % SYMBOL_COUNT;
        float fractional = symbolPos - (int) symbolPos;

        // We need to draw enough symbols to fill the view. The center symbol
        // is partially scrolled; we draw it and its neighbors above/below.
        // fractional is how far the center symbol has scrolled past center
        // (0 = perfectly centered, approaching 1 = almost scrolled away).
        // The symbol at centerIdx is offset upward by (fractional * cellH).

        // Draw symbols: from 1 above center to 1 below center (3 visible)
        for (int rel = -1; rel <= 1; rel++) {
            int idx = ((centerIdx + rel) % SYMBOL_COUNT + SYMBOL_COUNT) % SYMBOL_COUNT;
            float yOffset = (rel - fractional) * cellH;
            drawSymbol(canvas, idx, yOffset);
        }

        if (t >= 1f) {
            // Animation complete - snap to final state
            spinning = false;
            symbolIndex = targetSymbol;
            targetSymbol = -1;
            invalidate();
            if (onStopCallback != null) {
                Runnable cb = onStopCallback;
                onStopCallback = null;
                cb.run();
            }
        } else {
            // Continue animation
            postInvalidateOnAnimation();
        }
    }

    /**
     * Draw a single symbol at a vertical offset from the view's top.
     *
     * @param canvas  the canvas
     * @param idx     symbol index (0-5)
     * @param yOffset vertical pixel offset (0 = top of view, negative = above)
     */
    private void drawSymbol(Canvas canvas, int idx, float yOffset) {
        float w = getWidth();
        float h = getHeight();

        // Clip to view bounds so symbols don't overflow
        canvas.save();
        canvas.clipRect(0, 0, w, h);

        // Use a square region centered in the cell so sprites keep their
        // aspect ratio regardless of view width vs height.
        float side = Math.min(w, h);
        float left = (w - side) / 2f;
        float top = yOffset + (h - side) / 2f;
        float padding = side * 0.1f;

        Bitmap icon = (idx >= 0 && idx < cachedIcons.length) ? cachedIcons[idx] : null;
        if (icon != null && !icon.isRecycled()) {
            paint.setFilterBitmap(false);
            canvas.drawBitmap(icon, null,
                new RectF(left + padding, top + padding,
                          left + side - padding, top + side - padding), paint);
        } else if (idx >= 0 && idx < SYMBOL_COLORS.length) {
            // Fallback: rarity-colored circle
            paint.setColor(SYMBOL_COLORS[idx]);
            paint.setStyle(Paint.Style.FILL);
            float cx = w / 2f;
            float cy = yOffset + h / 2f;
            float radius = side / 3f;
            canvas.drawCircle(cx, cy, radius, paint);
        }

        canvas.restore();
    }
}
