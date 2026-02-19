package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class BlueDress extends Item {

    public BlueDress() {
        super("Blue Dress", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("An elegant blue dress");
    }

    @Override
    public Item copy() {
        return new BlueDress();
    }
}
