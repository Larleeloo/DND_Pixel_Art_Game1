package com.ambermoongame.entity.item.items.tools;

import com.ambermoongame.entity.item.Item;

/**
 * Fishing Rod - For catching fish.
 * Common tool for fishing.
 */
public class FishingRod extends Item {

    public FishingRod() {
        super("Fishing Rod", ItemCategory.TOOL);
        setDamage(1);
        setRarity(ItemRarity.COMMON);
        setDescription("For catching fish");
    }

    @Override
    public Item copy() {
        return new FishingRod();
    }
}
