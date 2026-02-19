package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class GoldRubyNecklace extends Item {

    public GoldRubyNecklace() {
        super("Gold Ruby Necklace", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.EPIC);
        setDescription("A gold necklace set with a ruby");
    }

    @Override
    public Item copy() {
        return new GoldRubyNecklace();
    }
}
