package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class RedPants extends Item {

    public RedPants() {
        super("Red Pants", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of red pants");
    }

    @Override
    public Item copy() {
        return new RedPants();
    }
}
