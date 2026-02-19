package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class GoldRubyBracelet extends Item {

    public GoldRubyBracelet() {
        super("Gold Ruby Bracelet", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.EPIC);
        setDescription("A gold bracelet set with a ruby");
    }

    @Override
    public Item copy() {
        return new GoldRubyBracelet();
    }
}
