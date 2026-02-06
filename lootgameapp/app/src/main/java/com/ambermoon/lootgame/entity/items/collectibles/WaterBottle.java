package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Water Bottle - Clean drinking water.
 * Common collectible item.
 */
public class WaterBottle extends Item {

    public WaterBottle() {
        super("Water Bottle", ItemCategory.OTHER);
        setRarity(ItemRarity.COMMON);
        setDescription("Clean drinking water");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new WaterBottle();
    }
}
