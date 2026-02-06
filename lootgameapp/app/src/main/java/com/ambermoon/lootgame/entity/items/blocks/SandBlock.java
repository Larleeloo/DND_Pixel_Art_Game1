package com.ambermoon.lootgame.entity.items.blocks;

import com.ambermoon.lootgame.entity.Item;

/**
 * Sand Block - A block of sand.
 * Common placeable block item.
 */
public class SandBlock extends Item {

    public SandBlock() {
        super("Sand Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A block of sand");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new SandBlock();
    }
}
