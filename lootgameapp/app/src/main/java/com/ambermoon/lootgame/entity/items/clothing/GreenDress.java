package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

/**
 * Green Dress - An elegant green gown.
 * Common cosmetic clothing item.
 */
public class GreenDress extends Item {

    public GreenDress() {
        super("Green Dress", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("An elegant green gown");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new GreenDress();
    }
}
