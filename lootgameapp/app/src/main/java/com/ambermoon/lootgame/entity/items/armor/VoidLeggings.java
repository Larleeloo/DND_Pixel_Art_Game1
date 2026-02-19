package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class VoidLeggings extends Item {

    public VoidLeggings() {
        super("Void Leggings", ItemCategory.ARMOR);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Leggings forged from void essence");
        setDefense(22);
        setSpecialEffect("Void agility");
    }

    @Override
    public Item copy() {
        return new VoidLeggings();
    }
}
