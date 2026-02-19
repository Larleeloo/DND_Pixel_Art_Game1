package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class PurpleHat extends Item {

    public PurpleHat() {
        super("Purple Hat", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A fashionable purple hat");
    }

    @Override
    public Item copy() {
        return new PurpleHat();
    }
}
