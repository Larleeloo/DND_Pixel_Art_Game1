package com.ambermoon.lootgame.entity.items.blocks;

import com.ambermoon.lootgame.entity.Item;

/**
 * Snow Block - A block of packed snow.
 * Common placeable block item.
 */
public class SnowBlock extends Item {

    public SnowBlock() {
        super("Snow Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A block of packed snow");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new SnowBlock();
    }
}
