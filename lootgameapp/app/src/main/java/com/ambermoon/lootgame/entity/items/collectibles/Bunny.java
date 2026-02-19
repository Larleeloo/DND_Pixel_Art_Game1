package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

public class Bunny extends Item {

    public Bunny() {
        super("Bunny", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("An adorable little bunny");
    }

    @Override
    public Item copy() {
        return new Bunny();
    }
}
