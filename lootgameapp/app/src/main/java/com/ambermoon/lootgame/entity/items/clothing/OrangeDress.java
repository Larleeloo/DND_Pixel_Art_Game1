package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

/**
 * Orange Dress - A vibrant orange dress.
 * Common cosmetic clothing item.
 */
public class OrangeDress extends Item {

    public OrangeDress() {
        super("Orange Dress", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A vibrant orange dress");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new OrangeDress();
    }
}
