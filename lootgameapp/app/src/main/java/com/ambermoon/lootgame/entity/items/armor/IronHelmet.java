package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

/**
 * Iron Helmet - Basic head protection.
 * Common armor piece with moderate defense.
 */
public class IronHelmet extends Item {

    public IronHelmet() {
        super("Iron Helmet", ItemCategory.ARMOR);
        setDefense(5);
        setRarity(ItemRarity.COMMON);
        setDescription("Basic head protection");
    }

    @Override
    public Item copy() {
        return new IronHelmet();
    }
}
