package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class PalmSapling extends Item {

    public PalmSapling() {
        super("Palm Sapling", ItemCategory.MATERIAL);
        setRarity(ItemRarity.RARE);
        setDescription("A young palm tree sapling");
    }

    @Override
    public Item copy() {
        return new PalmSapling();
    }
}
