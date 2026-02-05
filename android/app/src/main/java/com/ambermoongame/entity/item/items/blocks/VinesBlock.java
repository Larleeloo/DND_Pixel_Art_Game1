package com.ambermoongame.entity.item.items.blocks;

import com.ambermoongame.entity.item.Item;

/**
 * Vines Block - A block of hanging vines.
 * Common placeable block item (non-solid).
 */
public class VinesBlock extends Item {

    public VinesBlock() {
        super("Vines Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A block of hanging vines");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new VinesBlock();
    }
}
