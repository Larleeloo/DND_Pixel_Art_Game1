package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class GoldEmeraldBracelet extends Item {

    public GoldEmeraldBracelet() {
        super("Gold Emerald Bracelet", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.EPIC);
        setDescription("A gold bracelet set with a emerald");
    }

    @Override
    public Item copy() {
        return new GoldEmeraldBracelet();
    }
}
