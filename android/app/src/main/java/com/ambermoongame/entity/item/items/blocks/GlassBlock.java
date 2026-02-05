package com.ambermoongame.entity.item.items.blocks;

import com.ambermoongame.entity.item.Item;

/**
 * Glass Block - A transparent glass block.
 * Uncommon placeable block item.
 */
public class GlassBlock extends Item {

    public GlassBlock() {
        super("Glass Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A transparent glass block");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new GlassBlock();
    }
}
