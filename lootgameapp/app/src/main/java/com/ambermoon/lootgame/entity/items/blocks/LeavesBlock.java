package com.ambermoon.lootgame.entity.items.blocks;

import com.ambermoon.lootgame.entity.Item;

/**
 * Leaves Block - A cluster of leaves.
 * Common placeable block item.
 */
public class LeavesBlock extends Item {

    public LeavesBlock() {
        super("Leaves Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A cluster of leaves");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new LeavesBlock();
    }
}
