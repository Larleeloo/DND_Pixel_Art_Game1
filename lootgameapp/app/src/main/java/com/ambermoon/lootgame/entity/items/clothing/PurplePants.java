package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class PurplePants extends Item {

    public PurplePants() {
        super("Purple Pants", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of purple pants");
    }

    @Override
    public Item copy() {
        return new PurplePants();
    }
}
