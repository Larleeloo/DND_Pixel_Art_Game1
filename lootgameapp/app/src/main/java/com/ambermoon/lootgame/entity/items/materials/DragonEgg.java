package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

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
