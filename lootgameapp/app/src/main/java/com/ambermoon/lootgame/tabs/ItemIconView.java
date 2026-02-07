package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.*;
import android.view.View;

import com.ambermoon.lootgame.entity.Item;

/**
 * Custom View that draws an item icon with rarity border.
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
        Paint p = new Paint();
        // Background
        p.setColor(Color.parseColor("#1A1525"));
        canvas.drawRect(0, 0, getWidth(), getHeight(), p);
        // Rarity border
        p.setColor(Item.getRarityColor(item.getRarity().ordinal()));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4);
        canvas.drawRect(2, 2, getWidth() - 2, getHeight() - 2, p);
        // Item icon bitmap if available
        Bitmap icon = item.getIcon();
        if (icon != null) {
            canvas.drawBitmap(icon, null, new RectF(6, 6, getWidth() - 6, getHeight() - 6), null);
        } else {
            // Placeholder text
            p.setStyle(Paint.Style.FILL);
            p.setColor(Item.getRarityColor(item.getRarity().ordinal()));
            p.setTextSize(28);
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(item.getName().substring(0, Math.min(2, item.getName().length())),
                    getWidth() / 2f, getHeight() / 2f + 10, p);
        }
    }
}
