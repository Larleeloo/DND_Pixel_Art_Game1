package com.ambermoon.lootgame.entity.items.blocks;

import com.ambermoon.lootgame.entity.Item;

/**
 * Cobblestone Block - A rough cobblestone block.
 * Common placeable block item.
 */
public class CobblestoneBlock extends Item {

    public CobblestoneBlock() {
        super("Cobblestone Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A rough cobblestone block");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new CobblestoneBlock();
    }
}
