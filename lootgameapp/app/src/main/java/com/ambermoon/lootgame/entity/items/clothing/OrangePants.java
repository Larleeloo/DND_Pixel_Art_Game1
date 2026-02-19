package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class OrangePants extends Item {

    public OrangePants() {
        super("Orange Pants", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of orange pants");
    }

    @Override
    public Item copy() {
        return new OrangePants();
    }
}
