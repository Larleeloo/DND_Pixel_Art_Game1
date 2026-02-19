package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Rhododendron extends Item {

    public Rhododendron() {
        super("Rhododendron", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A beautiful flowering shrub");
    }

    @Override
    public Item copy() {
        return new Rhododendron();
    }
}
