package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

/**
 * Sapling - A young tree ready to plant.
 * Common planting material.
 */
public class Sapling extends Item {

    public Sapling() {
        super("Sapling", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A young tree ready to plant");
    }

    @Override
    public Item copy() {
        return new Sapling();
    }
}
