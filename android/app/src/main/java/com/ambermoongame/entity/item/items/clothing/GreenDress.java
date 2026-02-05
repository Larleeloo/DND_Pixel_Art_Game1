package com.ambermoongame.entity.item.items.clothing;

import com.ambermoongame.entity.item.Item;

/**
 * Green Dress - An elegant green gown.
 * Common cosmetic clothing item.
 */
public class GreenDress extends Item {

    public GreenDress() {
        super("Green Dress", ItemCategory.CLOTHING);
        setRarity(ItemRarity.COMMON);
        setDescription("An elegant green gown");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new GreenDress();
    }
}
