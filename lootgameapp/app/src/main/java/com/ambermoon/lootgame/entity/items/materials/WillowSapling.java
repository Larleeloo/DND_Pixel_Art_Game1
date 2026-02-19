package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class WillowSapling extends Item {

    public WillowSapling() {
        super("Willow Sapling", ItemCategory.MATERIAL);
        setRarity(ItemRarity.RARE);
        setDescription("A young willow tree sapling");
    }

    @Override
    public Item copy() {
        return new WillowSapling();
    }
}
