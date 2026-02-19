package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class RedRobe extends Item {

    public RedRobe() {
        super("Red Robe", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Flowing red robes");
    }

    @Override
    public Item copy() {
        return new RedRobe();
    }
}
