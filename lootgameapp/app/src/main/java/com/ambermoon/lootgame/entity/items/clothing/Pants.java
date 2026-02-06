package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

/**
 * Pants - Basic comfortable trousers.
 * Common cosmetic clothing item.
 */
public class Pants extends Item {

    public Pants() {
        super("Pants", ItemCategory.CLOTHING);
        setRarity(ItemRarity.COMMON);
        setDescription("Basic comfortable trousers");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new Pants();
    }
}
