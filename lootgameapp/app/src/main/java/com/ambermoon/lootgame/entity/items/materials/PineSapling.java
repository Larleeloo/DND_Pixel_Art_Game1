package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class PineSapling extends Item {

    public PineSapling() {
        super("Pine Sapling", ItemCategory.MATERIAL);
        setRarity(ItemRarity.RARE);
        setDescription("A young pine tree sapling");
    }

    @Override
    public Item copy() {
        return new PineSapling();
    }
}
