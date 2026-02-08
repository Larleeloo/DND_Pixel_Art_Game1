package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.*;
import android.view.View;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ItemRegistry;
import com.ambermoon.lootgame.graphics.AssetLoader;

/**
 * Custom View that renders a single slot machine reel.
 * Displays static item images (first frame of idle GIF) for each symbol.
 * Falls back to a rarity-colored circle if the image is not available.
 *
 * Slot symbols map to item IDs:
 *   Apple  -> "apple"
 *   Sword  -> "iron_sword"
 *   Shield -> "iron_shield" (or fallback)
 *   Gem    -> "diamond"
 *   Star   -> "magic_crystal"
 *   Crown  -> "ancient_crown"
 *
 * Top-level class to avoid D8 dex compiler crash on inner classes.
 */
public class SlotReelView extends View {
    /** Item IDs corresponding to each symbol index. */
    public static final String[] SYMBOL_ITEM_IDS = {
        "apple", "iron_sword", "steel_shield", "diamond", "magic_crystal", "ancient_crown"
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

    private int symbolIndex = -1;  // -1 means "?" state
    private boolean showSpinning = false;
    private Bitmap[] cachedIcons;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

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

    /** Set the displayed symbol (0-5), or -1 for "?" state. */
    public void setSymbol(int symbolIndex) {
        this.symbolIndex = symbolIndex;
        this.showSpinning = false;
        invalidate();
    }

    /** Show spinning state (blur indicator). */
    public void setSpinning() {
        this.showSpinning = true;
        this.symbolIndex = -1;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (showSpinning) {
            // Draw spinning indicator bars
            paint.setColor(Color.parseColor("#444466"));
            paint.setStyle(Paint.Style.FILL);
            float barH = getHeight() / 6f;
            for (int i = 0; i < 3; i++) {
                float y = getHeight() / 4f + i * barH;
                canvas.drawRect(getWidth() * 0.2f, y, getWidth() * 0.8f, y + barH * 0.6f, paint);
            }
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

        Bitmap icon = cachedIcons[symbolIndex];
        if (icon != null && !icon.isRecycled()) {
            // Draw item sprite image
            float padding = getWidth() * 0.1f;
            paint.setFilterBitmap(false);
            canvas.drawBitmap(icon, null,
                new RectF(padding, padding, getWidth() - padding, getHeight() - padding), paint);
        } else {
            // Fallback: rarity-colored circle
            paint.setColor(SYMBOL_COLORS[symbolIndex]);
            paint.setStyle(Paint.Style.FILL);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float radius = Math.min(getWidth(), getHeight()) / 3f;
            canvas.drawCircle(cx, cy, radius, paint);
        }
    }
}
