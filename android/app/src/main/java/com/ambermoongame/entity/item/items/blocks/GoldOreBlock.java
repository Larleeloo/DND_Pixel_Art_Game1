package com.ambermoongame.entity.item.items.blocks;

import com.ambermoongame.entity.item.Item;

/**
 * Gold Ore Block - A block containing gold ore.
 * Uncommon placeable block item.
 */
public class GoldOreBlock extends Item {

    public GoldOreBlock() {
        super("Gold Ore Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A block of gold ore");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new GoldOreBlock();
    }
}
