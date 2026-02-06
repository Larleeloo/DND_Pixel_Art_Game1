package com.ambermoon.lootgame.entity.items.blocks;

import com.ambermoon.lootgame.entity.Item;

/**
 * Dirt Block - A block of dirt.
 * Common placeable block item.
 */
public class DirtBlock extends Item {

    public DirtBlock() {
        super("Dirt Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A block of dirt");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new DirtBlock();
    }
}
