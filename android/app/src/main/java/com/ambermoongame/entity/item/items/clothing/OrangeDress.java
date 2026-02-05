package com.ambermoongame.entity.item.items.clothing;

import com.ambermoongame.entity.item.Item;

/**
 * Orange Dress - A vibrant orange dress.
 * Common cosmetic clothing item.
 */
public class OrangeDress extends Item {

    public OrangeDress() {
        super("Orange Dress", ItemCategory.CLOTHING);
        setRarity(ItemRarity.COMMON);
        setDescription("A vibrant orange dress");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new OrangeDress();
    }
}
