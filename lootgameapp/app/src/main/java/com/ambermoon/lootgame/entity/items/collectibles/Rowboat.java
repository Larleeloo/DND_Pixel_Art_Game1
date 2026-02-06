package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Rowboat - For crossing water.
 * Uncommon collectible item.
 */
public class Rowboat extends Item {

    public Rowboat() {
        super("Rowboat", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("For crossing water");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Rowboat();
    }
}
