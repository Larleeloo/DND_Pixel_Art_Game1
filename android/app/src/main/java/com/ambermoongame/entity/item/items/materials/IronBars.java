package com.ambermoongame.entity.item.items.materials;

import com.ambermoongame.entity.item.Item;

/**
 * Iron Bars - Refined iron metal.
 * Common crafting material.
 */
public class IronBars extends Item {

    public IronBars() {
        super("Iron Bars", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Refined iron metal");
    }

    @Override
    public Item copy() {
        return new IronBars();
    }
}
