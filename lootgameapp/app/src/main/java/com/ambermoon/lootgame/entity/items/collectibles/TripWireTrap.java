package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Trip-Wire Trap - Catches unsuspecting foes.
 * Uncommon collectible item.
 */
public class TripWireTrap extends Item {

    public TripWireTrap() {
        super("Trip-Wire Trap", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("Catches unsuspecting foes");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new TripWireTrap();
    }
}
