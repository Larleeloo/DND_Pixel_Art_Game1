package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class RedHat extends Item {

    public RedHat() {
        super("Red Hat", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A fashionable red hat");
    }

    @Override
    public Item copy() {
        return new RedHat();
    }
}
