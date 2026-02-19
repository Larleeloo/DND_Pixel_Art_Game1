package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class OrangeRobe extends Item {

    public OrangeRobe() {
        super("Orange Robe", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Flowing orange robes");
    }

    @Override
    public Item copy() {
        return new OrangeRobe();
    }
}
