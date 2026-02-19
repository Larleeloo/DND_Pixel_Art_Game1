package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class VoidChestplate extends Item {

    public VoidChestplate() {
        super("Void Chestplate", ItemCategory.ARMOR);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A chestplate forged from void essence");
        setDefense(28);
        setSpecialEffect("Void resistance");
    }

    @Override
    public Item copy() {
        return new VoidChestplate();
    }
}
