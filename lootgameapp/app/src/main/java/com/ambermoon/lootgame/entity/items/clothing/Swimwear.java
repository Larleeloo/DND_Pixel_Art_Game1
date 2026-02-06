package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

/**
 * Swimwear - Perfect for beach adventures.
 * Common cosmetic clothing item.
 */
public class Swimwear extends Item {

    public Swimwear() {
        super("Swimwear", ItemCategory.CLOTHING);
        setRarity(ItemRarity.COMMON);
        setDescription("Perfect for beach adventures");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new Swimwear();
    }
}
