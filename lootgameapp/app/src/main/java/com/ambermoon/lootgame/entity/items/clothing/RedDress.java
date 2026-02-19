package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class RedDress extends Item {

    public RedDress() {
        super("Red Dress", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("An elegant red dress");
    }

    @Override
    public Item copy() {
        return new RedDress();
    }
}
