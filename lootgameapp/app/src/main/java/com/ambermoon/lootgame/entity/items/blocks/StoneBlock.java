package com.ambermoon.lootgame.entity.items.blocks;

import com.ambermoon.lootgame.entity.Item;

/**
 * Stone Block - A solid stone block.
 * Common placeable block item.
 */
public class StoneBlock extends Item {

    public StoneBlock() {
        super("Stone Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A solid stone block");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new StoneBlock();
    }
}
