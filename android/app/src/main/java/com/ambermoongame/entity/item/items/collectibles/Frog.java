package com.ambermoongame.entity.item.items.collectibles;

import com.ambermoongame.entity.item.Item;

/**
 * Frog - A small amphibian friend.
 * Common collectible item.
 */
public class Frog extends Item {

    public Frog() {
        super("Frog", ItemCategory.OTHER);
        setRarity(ItemRarity.COMMON);
        setDescription("A small amphibian friend");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Frog();
    }
}
