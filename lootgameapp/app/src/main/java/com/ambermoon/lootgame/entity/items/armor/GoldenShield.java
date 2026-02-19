package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class GoldenShield extends Item {

    public GoldenShield() {
        super("Golden Shield", ItemCategory.ARMOR);
        setRarity(ItemRarity.EPIC);
        setDescription("A magnificent golden shield");
        setDefense(18);
    }

    @Override
    public Item copy() {
        return new GoldenShield();
    }
}
