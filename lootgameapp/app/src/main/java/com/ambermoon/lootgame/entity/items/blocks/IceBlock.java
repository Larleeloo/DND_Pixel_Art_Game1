package com.ambermoon.lootgame.entity.items.blocks;

import com.ambermoon.lootgame.entity.Item;

/**
 * Ice Block - A block of solid ice.
 * Common placeable block item (non-solid/transparent).
 */
public class IceBlock extends Item {

    public IceBlock() {
        super("Ice Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A block of solid ice");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new IceBlock();
    }
}
