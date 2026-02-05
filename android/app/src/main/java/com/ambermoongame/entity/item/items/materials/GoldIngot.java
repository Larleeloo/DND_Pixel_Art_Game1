package com.ambermoongame.entity.item.items.materials;

import com.ambermoongame.entity.item.Item;

/**
 * Gold Ingot - Precious metal.
 * Uncommon crafting material.
 */
public class GoldIngot extends Item {

    public GoldIngot() {
        super("Gold Ingot", ItemCategory.MATERIAL);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Precious metal");
    }

    @Override
    public Item copy() {
        return new GoldIngot();
    }
}
