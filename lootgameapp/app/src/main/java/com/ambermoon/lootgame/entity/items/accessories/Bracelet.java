package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class Bracelet extends Item {

    public Bracelet() {
        super("Bracelet", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.COMMON);
        setDescription("A simple bracelet");
    }

    @Override
    public Item copy() {
        return new Bracelet();
    }
}
