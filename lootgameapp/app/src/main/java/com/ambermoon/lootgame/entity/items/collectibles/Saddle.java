package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Saddle - For riding mounts.
 * Uncommon collectible item.
 */
public class Saddle extends Item {

    public Saddle() {
        super("Saddle", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("For riding mounts");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Saddle();
    }
}
