package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class GoldNecklace extends Item {

    public GoldNecklace() {
        super("Gold Necklace", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.RARE);
        setDescription("A necklace of pure gold");
    }

    @Override
    public Item copy() {
        return new GoldNecklace();
    }
}
