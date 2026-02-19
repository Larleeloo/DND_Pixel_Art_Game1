package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class CherrySapling extends Item {

    public CherrySapling() {
        super("Cherry Sapling", ItemCategory.MATERIAL);
        setRarity(ItemRarity.RARE);
        setDescription("A young cherry tree sapling");
    }

    @Override
    public Item copy() {
        return new CherrySapling();
    }
}
