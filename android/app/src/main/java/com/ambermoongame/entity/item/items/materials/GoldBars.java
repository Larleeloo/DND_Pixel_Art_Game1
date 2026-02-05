package com.ambermoongame.entity.item.items.materials;

import com.ambermoongame.entity.item.Item;

/**
 * Gold Bars - Refined gold, highly valuable.
 * Uncommon crafting material.
 */
public class GoldBars extends Item {

    public GoldBars() {
        super("Gold Bars", ItemCategory.MATERIAL);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Refined gold, highly valuable");
    }

    @Override
    public Item copy() {
        return new GoldBars();
    }
}
