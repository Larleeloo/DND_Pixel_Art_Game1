package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

public class LegendaryForge extends Item {

    public LegendaryForge() {
        super("Legendary Forge", ItemCategory.OTHER);
        setRarity(ItemRarity.EPIC);
        setDescription("A portable forge of legendary craftsmanship");
    }

    @Override
    public Item copy() {
        return new LegendaryForge();
    }
}
