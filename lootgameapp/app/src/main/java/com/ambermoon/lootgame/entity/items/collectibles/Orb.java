package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Orb - A mysterious glowing sphere.
 * Uncommon collectible item.
 */
public class Orb extends Item {

    public Orb() {
        super("Orb", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A mysterious glowing sphere");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Orb();
    }
}
