package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Violet extends Item {

    public Violet() {
        super("Violet", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A delicate purple flower");
    }

    @Override
    public Item copy() {
        return new Violet();
    }
}
