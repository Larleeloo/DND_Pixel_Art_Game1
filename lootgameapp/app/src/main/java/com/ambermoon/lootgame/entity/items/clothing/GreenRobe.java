package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class GreenRobe extends Item {

    public GreenRobe() {
        super("Green Robe", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Flowing green robes");
    }

    @Override
    public Item copy() {
        return new GreenRobe();
    }
}
