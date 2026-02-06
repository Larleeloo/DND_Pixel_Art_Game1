package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Candle - Provides light in darkness.
 * Common collectible item.
 */
public class Candle extends Item {

    public Candle() {
        super("Candle", ItemCategory.OTHER);
        setRarity(ItemRarity.COMMON);
        setDescription("Provides light in darkness");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Candle();
    }
}
