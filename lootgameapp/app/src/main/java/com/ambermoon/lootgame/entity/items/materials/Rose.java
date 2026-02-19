package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Rose extends Item {

    public Rose() {
        super("Rose", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A fragrant red rose");
    }

    @Override
    public Item copy() {
        return new Rose();
    }
}
