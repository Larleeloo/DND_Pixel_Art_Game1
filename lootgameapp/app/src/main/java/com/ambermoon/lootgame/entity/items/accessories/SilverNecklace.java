package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class SilverNecklace extends Item {

    public SilverNecklace() {
        super("Silver Necklace", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A polished silver necklace");
    }

    @Override
    public Item copy() {
        return new SilverNecklace();
    }
}
