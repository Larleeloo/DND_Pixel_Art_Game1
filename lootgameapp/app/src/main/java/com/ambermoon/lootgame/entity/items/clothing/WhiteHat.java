package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class WhiteHat extends Item {

    public WhiteHat() {
        super("White Hat", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A fashionable white hat");
    }

    @Override
    public Item copy() {
        return new WhiteHat();
    }
}
