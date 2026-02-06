package com.ambermoon.lootgame.entity.items.blocks;

import com.ambermoon.lootgame.entity.Item;

/**
 * Grass Block - A grass-covered dirt block.
 * Common placeable block item.
 */
public class GrassBlock extends Item {

    public GrassBlock() {
        super("Grass Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A grass-covered dirt block");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new GrassBlock();
    }
}
