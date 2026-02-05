package com.ambermoongame.entity.item.items.collectibles;

import com.ambermoongame.entity.item.Item;

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
