package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class SilverRubyBracelet extends Item {

    public SilverRubyBracelet() {
        super("Silver Ruby Bracelet", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.RARE);
        setDescription("A silver bracelet set with a ruby");
    }

    @Override
    public Item copy() {
        return new SilverRubyBracelet();
    }
}
