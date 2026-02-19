package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class GoldBracelet extends Item {

    public GoldBracelet() {
        super("Gold Bracelet", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.RARE);
        setDescription("A bracelet of pure gold");
    }

    @Override
    public Item copy() {
        return new GoldBracelet();
    }
}
