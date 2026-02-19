package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class SilverRubyNecklace extends Item {

    public SilverRubyNecklace() {
        super("Silver Ruby Necklace", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.RARE);
        setDescription("A silver necklace set with a ruby");
    }

    @Override
    public Item copy() {
        return new SilverRubyNecklace();
    }
}
