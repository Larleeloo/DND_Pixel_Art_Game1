package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Ruby extends Item {

    public Ruby() {
        super("Ruby", ItemCategory.MATERIAL);
        setRarity(ItemRarity.RARE);
        setDescription("A brilliant red gemstone");
    }

    @Override
    public Item copy() {
        return new Ruby();
    }
}
