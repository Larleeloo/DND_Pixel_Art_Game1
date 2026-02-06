package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

/**
 * Ink - Used for writing and enchanting.
 * Common crafting material.
 */
public class Ink extends Item {

    public Ink() {
        super("Ink", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Used for writing and enchanting");
    }

    @Override
    public Item copy() {
        return new Ink();
    }
}
