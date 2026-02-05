package com.ambermoongame.entity.item.items.collectibles;

import com.ambermoongame.entity.item.Item;

/**
 * Rocket - Launches into the sky.
 * Uncommon collectible item.
 */
public class Rocket extends Item {

    public Rocket() {
        super("Rocket", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Launches into the sky");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Rocket();
    }
}
