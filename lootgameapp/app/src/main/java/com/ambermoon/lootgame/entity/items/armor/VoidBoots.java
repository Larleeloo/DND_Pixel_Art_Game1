package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class VoidBoots extends Item {

    public VoidBoots() {
        super("Void Boots", ItemCategory.ARMOR);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Boots forged from void essence");
        setDefense(18);
        setSpecialEffect("Void step");
    }

    @Override
    public Item copy() {
        return new VoidBoots();
    }
}
