package com.ambermoongame.entity.item.items.collectibles;

import com.ambermoongame.entity.item.Item;

/**
 * Ancient Pottery - A relic from ancient times.
 * Uncommon collectible item.
 */
public class AncientPottery extends Item {

    public AncientPottery() {
        super("Ancient Pottery", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A relic from ancient times");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new AncientPottery();
    }
}
