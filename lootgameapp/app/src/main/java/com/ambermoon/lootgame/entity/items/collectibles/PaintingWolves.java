package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Painting of 3 Wolves - A beautiful wolf painting.
 * Uncommon collectible item.
 */
public class PaintingWolves extends Item {

    public PaintingWolves() {
        super("Painting of 3 Wolves", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A beautiful wolf painting");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new PaintingWolves();
    }
}
