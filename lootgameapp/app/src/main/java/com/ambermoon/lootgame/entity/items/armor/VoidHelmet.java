package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class VoidHelmet extends Item {

    public VoidHelmet() {
        super("Void Helmet", ItemCategory.ARMOR);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A helmet forged from void essence");
        setDefense(20);
        setSpecialEffect("Void sight");
    }

    @Override
    public Item copy() {
        return new VoidHelmet();
    }
}
