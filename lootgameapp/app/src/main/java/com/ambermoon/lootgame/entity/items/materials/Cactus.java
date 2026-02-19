package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Cactus extends Item {

    public Cactus() {
        super("Cactus", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A prickly desert plant");
    }

    @Override
    public Item copy() {
        return new Cactus();
    }
}
