package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class GoldDiamondNecklace extends Item {

    public GoldDiamondNecklace() {
        super("Gold Diamond Necklace", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.EPIC);
        setDescription("A gold necklace set with a diamond");
    }

    @Override
    public Item copy() {
        return new GoldDiamondNecklace();
    }
}
