package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Mysterious Candle - Burns with an ethereal flame.
 * Rare collectible item.
 */
public class MysteriousCandle extends Item {

    public MysteriousCandle() {
        super("Mysterious Candle", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("Burns with an ethereal flame");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new MysteriousCandle();
    }
}
