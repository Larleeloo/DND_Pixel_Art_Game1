package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Journal - A book for recording thoughts.
 * Common collectible item.
 */
public class Journal extends Item {

    public Journal() {
        super("Journal", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A book for recording thoughts");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Journal();
    }
}
