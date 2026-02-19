package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class OrangeHat extends Item {

    public OrangeHat() {
        super("Orange Hat", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A fashionable orange hat");
    }

    @Override
    public Item copy() {
        return new OrangeHat();
    }
}
