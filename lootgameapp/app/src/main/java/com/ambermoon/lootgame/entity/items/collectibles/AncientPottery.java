package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Ancient Pottery - A relic from ancient times.
 * Uncommon collectible item.
 */
public class AncientPottery extends Item {

    public AncientPottery() {
        super("Ancient Pottery", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("A relic from ancient times");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new AncientPottery();
    }
}
