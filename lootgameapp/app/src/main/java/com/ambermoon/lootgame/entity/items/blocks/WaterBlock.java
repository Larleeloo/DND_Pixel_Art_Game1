package com.ambermoon.lootgame.entity.items.blocks;

import com.ambermoon.lootgame.entity.Item;

/**
 * Water Block - A block of water.
 * Common placeable block item (non-solid).
 */
public class WaterBlock extends Item {

    public WaterBlock() {
        super("Water Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A block of water");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new WaterBlock();
    }
}
