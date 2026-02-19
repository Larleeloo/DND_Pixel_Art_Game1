package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class PlatinumCrown extends Item {

    public PlatinumCrown() {
        super("Platinum Crown", ItemCategory.CLOTHING);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A crown of pure platinum");
        setSpecialEffect("Inspire allies");
    }

    @Override
    public Item copy() {
        return new PlatinumCrown();
    }
}
