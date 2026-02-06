package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

/**
 * Leather - Tanned animal hide.
 * Common crafting material.
 */
public class Leather extends Item {

    public Leather() {
        super("Leather", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Tanned animal hide");
    }

    @Override
    public Item copy() {
        return new Leather();
    }
}
