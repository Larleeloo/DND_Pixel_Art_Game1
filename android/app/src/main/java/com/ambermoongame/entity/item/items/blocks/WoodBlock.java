package com.ambermoongame.entity.item.items.blocks;

import com.ambermoongame.entity.item.Item;

/**
 * Wood Block - A wooden block.
 * Common placeable block item.
 */
public class WoodBlock extends Item {

    public WoodBlock() {
        super("Wood Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A wooden block");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new WoodBlock();
    }
}
