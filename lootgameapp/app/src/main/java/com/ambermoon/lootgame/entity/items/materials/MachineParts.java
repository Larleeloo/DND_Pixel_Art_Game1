package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

/**
 * Machine Parts - Gears and components.
 * Uncommon crafting material.
 */
public class MachineParts extends Item {

    public MachineParts() {
        super("Machine Parts", ItemCategory.MATERIAL);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Gears and components");
    }

    @Override
    public Item copy() {
        return new MachineParts();
    }
}
