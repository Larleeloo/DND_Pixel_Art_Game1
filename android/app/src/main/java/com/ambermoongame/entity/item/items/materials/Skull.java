package com.ambermoongame.entity.item.items.materials;

import com.ambermoongame.entity.item.Item;

/**
 * Skull - A grim reminder of mortality.
 * Uncommon crafting material.
 */
public class Skull extends Item {

    public Skull() {
        super("Skull", ItemCategory.MATERIAL);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A grim reminder of mortality");
    }

    @Override
    public Item copy() {
        return new Skull();
    }
}
