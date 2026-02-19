package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

/**
 * Iron Chestplate - Basic chest protection.
 * Common armor piece with moderate defense.
 */
public class IronChestplate extends Item {

    public IronChestplate() {
        super("Iron Chestplate", ItemCategory.ARMOR);
        setDefense(10);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Basic chest protection");
    }

    @Override
    public Item copy() {
        return new IronChestplate();
    }
}
