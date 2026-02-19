package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class WhiteDress extends Item {

    public WhiteDress() {
        super("White Dress", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("An elegant white dress");
    }

    @Override
    public Item copy() {
        return new WhiteDress();
    }
}
