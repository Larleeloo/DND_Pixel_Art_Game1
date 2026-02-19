package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Coal extends Item {

    public Coal() {
        super("Coal", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A chunk of coal");
    }

    @Override
    public Item copy() {
        return new Coal();
    }
}
