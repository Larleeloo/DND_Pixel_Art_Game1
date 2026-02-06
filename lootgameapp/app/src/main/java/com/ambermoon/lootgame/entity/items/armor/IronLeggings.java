package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

/**
 * Iron Leggings - Basic leg protection.
 * Common armor piece with moderate defense.
 */
public class IronLeggings extends Item {

    public IronLeggings() {
        super("Iron Leggings", ItemCategory.ARMOR);
        setDefense(7);
        setRarity(ItemRarity.COMMON);
        setDescription("Basic leg protection");
    }

    @Override
    public Item copy() {
        return new IronLeggings();
    }
}
