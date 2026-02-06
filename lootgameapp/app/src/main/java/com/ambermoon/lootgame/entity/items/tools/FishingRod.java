package com.ambermoon.lootgame.entity.items.tools;

import com.ambermoon.lootgame.entity.Item;

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
