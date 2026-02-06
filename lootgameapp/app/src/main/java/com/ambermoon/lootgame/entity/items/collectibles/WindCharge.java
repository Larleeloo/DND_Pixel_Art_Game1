package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Wind Charge - A gust of captured wind.
 * Uncommon collectible item.
 */
public class WindCharge extends Item {

    public WindCharge() {
        super("Wind Charge", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A gust of captured wind");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new WindCharge();
    }
}
