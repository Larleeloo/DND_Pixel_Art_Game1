package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class VoidGauntlets extends Item {

    public VoidGauntlets() {
        super("Void Gauntlets", ItemCategory.ARMOR);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Gauntlets forged from void essence");
        setDefense(16);
        setSpecialEffect("Void grip");
    }

    @Override
    public Item copy() {
        return new VoidGauntlets();
    }
}
