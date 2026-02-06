package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

/**
 * Scroll - Contains ancient knowledge.
 * Uncommon crafting material.
 */
public class Scroll extends Item {

    public Scroll() {
        super("Scroll", ItemCategory.MATERIAL);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Contains ancient knowledge");
    }

    @Override
    public Item copy() {
        return new Scroll();
    }
}
