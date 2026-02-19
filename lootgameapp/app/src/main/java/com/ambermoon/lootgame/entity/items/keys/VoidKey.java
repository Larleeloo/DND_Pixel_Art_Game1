package com.ambermoon.lootgame.entity.items.keys;

import com.ambermoon.lootgame.entity.Item;

public class VoidKey extends Item {

    public VoidKey() {
        super("Void Key", ItemCategory.KEY);
        setRarity(ItemRarity.EPIC);
        setDescription("A key forged from void energy");
    }

    @Override
    public Item copy() {
        return new VoidKey();
    }
}
