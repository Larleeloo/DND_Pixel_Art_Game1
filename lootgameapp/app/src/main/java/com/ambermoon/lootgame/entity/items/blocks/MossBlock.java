package com.ambermoon.lootgame.entity.items.blocks;

import com.ambermoon.lootgame.entity.Item;

/**
 * Moss Block - A block covered in moss.
 * Common placeable block item.
 */
public class MossBlock extends Item {

    public MossBlock() {
        super("Moss Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A block covered in moss");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new MossBlock();
    }
}
