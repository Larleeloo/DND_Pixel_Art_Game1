package com.ambermoongame.entity.item.items.clothing;

import com.ambermoongame.entity.item.Item;

/**
 * Shirt - A simple cloth shirt.
 * Common cosmetic clothing item.
 */
public class Shirt extends Item {

    public Shirt() {
        super("Shirt", ItemCategory.CLOTHING);
        setRarity(ItemRarity.COMMON);
        setDescription("A simple cloth shirt");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new Shirt();
    }
}
