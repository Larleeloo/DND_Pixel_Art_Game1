package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

/**
 * Gold Bars - Refined gold, highly valuable.
 * Uncommon crafting material.
 */
public class GoldBars extends Item {

    public GoldBars() {
        super("Gold Bar", ItemCategory.MATERIAL);
        setRarity(ItemRarity.EPIC);
        setDescription("A bar of pure gold");
    }

    @Override
    public Item copy() {
        return new GoldBars();
    }
}
