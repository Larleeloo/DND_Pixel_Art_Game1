package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class WoodenShield extends Item {

    public WoodenShield() {
        super("Wooden Shield", ItemCategory.ARMOR);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A basic wooden shield");
        setDefense(4);
    }

    @Override
    public Item copy() {
        return new WoodenShield();
    }
}
