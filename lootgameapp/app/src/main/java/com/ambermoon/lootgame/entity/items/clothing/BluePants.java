package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class BluePants extends Item {

    public BluePants() {
        super("Blue Pants", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of blue pants");
    }

    @Override
    public Item copy() {
        return new BluePants();
    }
}
