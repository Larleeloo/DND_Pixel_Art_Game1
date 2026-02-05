package com.ambermoongame.entity.item.items.collectibles;

import com.ambermoongame.entity.item.Item;

/**
 * Demon Horn - Torn from a greater demon.
 * Epic collectible with dark damage bonus.
 */
public class DemonHorn extends Item {

    public DemonHorn() {
        super("Demon Horn", ItemCategory.OTHER);
        setRarity(ItemRarity.EPIC);
        setDescription("Torn from a greater demon");
        setSpecialEffect("+25% dark damage");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new DemonHorn();
    }
}
