package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

/**
 * Iron Ore - Raw iron ready for smelting.
 * Common material from mining.
 */
public class IronOre extends Item {

    public IronOre() {
        super("Iron Ore", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Raw iron ready for smelting");
    }

    @Override
    public Item copy() {
        return new IronOre();
    }
}
