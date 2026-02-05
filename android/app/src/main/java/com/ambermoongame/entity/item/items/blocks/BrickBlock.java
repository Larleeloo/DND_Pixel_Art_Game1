package com.ambermoongame.entity.item.items.blocks;

import com.ambermoongame.entity.item.Item;

/**
 * Brick Block - A brick block.
 * Common placeable block item.
 */
public class BrickBlock extends Item {

    public BrickBlock() {
        super("Brick Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A brick block");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new BrickBlock();
    }
}
