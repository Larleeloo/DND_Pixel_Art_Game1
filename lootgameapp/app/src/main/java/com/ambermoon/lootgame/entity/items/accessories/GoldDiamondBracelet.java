package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class GoldDiamondBracelet extends Item {

    public GoldDiamondBracelet() {
        super("Gold Diamond Bracelet", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.EPIC);
        setDescription("A gold bracelet set with a diamond");
    }

    @Override
    public Item copy() {
        return new GoldDiamondBracelet();
    }
}
