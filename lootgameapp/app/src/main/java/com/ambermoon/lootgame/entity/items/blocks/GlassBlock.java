package com.ambermoon.lootgame.entity.items.blocks;

import com.ambermoon.lootgame.entity.Item;

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
