package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class SilverBracelet extends Item {

    public SilverBracelet() {
        super("Silver Bracelet", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A shimmering silver bracelet");
    }

    @Override
    public Item copy() {
        return new SilverBracelet();
    }
}
