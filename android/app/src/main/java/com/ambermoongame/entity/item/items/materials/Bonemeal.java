package com.ambermoongame.entity.item.items.materials;

import com.ambermoongame.entity.item.Item;

/**
 * Bonemeal - Ground bones for fertilizer.
 * Common crafting material.
 */
public class Bonemeal extends Item {

    public Bonemeal() {
        super("Bonemeal", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Ground bones for fertilizer");
    }

    @Override
    public Item copy() {
        return new Bonemeal();
    }
}
