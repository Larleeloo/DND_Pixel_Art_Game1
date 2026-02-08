package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.*;
import android.view.View;

import com.ambermoon.lootgame.entity.Item;

/**
 * Custom View that draws an item icon with rarity border.
 * Renders the first frame of the item's idle GIF sprite.
 * Falls back to a rarity-colored circle if no icon is available.
 *
 * Top-level class to avoid D8 dex compiler crash on inner classes.
 */
public class ItemIconView extends View {
    private final Item item;

    public ItemIconView(Context context, Item item) {
        super(context);
        this.item = item;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        // Background
        p.setColor(Color.parseColor("#1A1525"));
        canvas.drawRect(0, 0, getWidth(), getHeight(), p);
        // Rarity border
        int rarityColor = Item.getRarityColor(item.getRarity().ordinal());
        p.setColor(rarityColor);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4);
        canvas.drawRect(2, 2, getWidth() - 2, getHeight() - 2, p);
        // Item icon bitmap if available (first frame of idle GIF)
        Bitmap icon = item.getIcon();
        if (icon != null && !icon.isRecycled()) {
            p.setStyle(Paint.Style.FILL);
            p.setFilterBitmap(false);
            canvas.drawBitmap(icon, null, new RectF(6, 6, getWidth() - 6, getHeight() - 6), p);
        } else {
            // Fallback: rarity-colored circle
            p.setStyle(Paint.Style.FILL);
            p.setColor(rarityColor);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float radius = Math.min(getWidth(), getHeight()) / 2f - 8;
            canvas.drawCircle(cx, cy, radius, p);
        }
    }
}
