package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class GreenDye extends Item {

    public GreenDye() {
        super("Green Dye", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A vial of green dye");
    }

    @Override
    public Item copy() {
        return new GreenDye();
    }
}
