package com.ambermoongame.entity.item.items.materials;

import com.ambermoongame.entity.item.Item;

/**
 * Flour - Ground grain for baking.
 * Common crafting material.
 */
public class Flour extends Item {

    public Flour() {
        super("Flour", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Ground grain for baking");
    }

    @Override
    public Item copy() {
        return new Flour();
    }
}
