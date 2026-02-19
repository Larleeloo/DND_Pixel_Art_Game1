package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class Boots extends Item {

    public Boots() {
        super("Boots", ItemCategory.CLOTHING);
        setRarity(ItemRarity.COMMON);
        setDescription("A sturdy pair of boots");
    }

    @Override
    public Item copy() {
        return new Boots();
    }
}
