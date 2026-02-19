package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class OakSapling extends Item {

    public OakSapling() {
        super("Oak Sapling", ItemCategory.MATERIAL);
        setRarity(ItemRarity.RARE);
        setDescription("A young oak tree sapling");
    }

    @Override
    public Item copy() {
        return new OakSapling();
    }
}
