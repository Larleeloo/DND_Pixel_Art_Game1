package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

/**
 * Planks - Cut wooden boards.
 * Common crafting material.
 */
public class Planks extends Item {

    public Planks() {
        super("Planks", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Cut wooden boards");
    }

    @Override
    public Item copy() {
        return new Planks();
    }
}
