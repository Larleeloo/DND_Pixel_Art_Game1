package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class PurpleRobe extends Item {

    public PurpleRobe() {
        super("Purple Robe", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Flowing purple robes");
    }

    @Override
    public Item copy() {
        return new PurpleRobe();
    }
}
