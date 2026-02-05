package com.ambermoongame.entity.item.items.materials;

import com.ambermoongame.entity.item.Item;

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
