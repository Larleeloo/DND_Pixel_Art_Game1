package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class GoldEmeraldNecklace extends Item {

    public GoldEmeraldNecklace() {
        super("Gold Emerald Necklace", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.EPIC);
        setDescription("A gold necklace set with a emerald");
    }

    @Override
    public Item copy() {
        return new GoldEmeraldNecklace();
    }
}
