package com.ambermoongame.entity.item.items.materials;

import com.ambermoongame.entity.item.Item;

/**
 * Copper Ingots - Refined copper metal.
 * Common crafting material.
 */
public class CopperIngots extends Item {

    public CopperIngots() {
        super("Copper Ingots", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Refined copper metal");
    }

    @Override
    public Item copy() {
        return new CopperIngots();
    }
}
