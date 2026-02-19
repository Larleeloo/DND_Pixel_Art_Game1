package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class PurpleDress extends Item {

    public PurpleDress() {
        super("Purple Dress", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("An elegant purple dress");
    }

    @Override
    public Item copy() {
        return new PurpleDress();
    }
}
