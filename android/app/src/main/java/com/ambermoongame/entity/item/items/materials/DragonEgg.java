package com.ambermoongame.entity.item.items.materials;

import com.ambermoongame.entity.item.Item;

/**
 * Dragon Egg - A dormant dragon embryo.
 * Legendary non-stackable material.
 */
public class DragonEgg extends Item {

    public DragonEgg() {
        super("Dragon Egg", ItemCategory.MATERIAL);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A dormant dragon embryo");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new DragonEgg();
    }
}
