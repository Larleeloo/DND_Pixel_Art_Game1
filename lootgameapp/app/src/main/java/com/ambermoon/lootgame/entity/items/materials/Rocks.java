package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

/**
 * Rocks - Simple stones.
 * Common crafting material.
 */
public class Rocks extends Item {

    public Rocks() {
        super("Rocks", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Simple stones");
    }

    @Override
    public Item copy() {
        return new Rocks();
    }
}
