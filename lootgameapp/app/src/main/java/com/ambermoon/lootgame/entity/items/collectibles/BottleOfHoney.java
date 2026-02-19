package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

public class BottleOfHoney extends Item {

    public BottleOfHoney() {
        super("Bottle of Honey", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A jar of sweet golden honey");
    }

    @Override
    public Item copy() {
        return new BottleOfHoney();
    }
}
