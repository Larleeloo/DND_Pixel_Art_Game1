package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class VoidShield extends Item {

    public VoidShield() {
        super("Void Shield", ItemCategory.ARMOR);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A shield formed from pure void energy");
        setDefense(25);
        setSpecialEffect("Absorbs void damage");
    }

    @Override
    public Item copy() {
        return new VoidShield();
    }
}
