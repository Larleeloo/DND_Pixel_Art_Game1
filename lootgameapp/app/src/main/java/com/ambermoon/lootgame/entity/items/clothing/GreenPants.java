package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class GreenPants extends Item {

    public GreenPants() {
        super("Green Pants", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of green pants");
    }

    @Override
    public Item copy() {
        return new GreenPants();
    }
}
