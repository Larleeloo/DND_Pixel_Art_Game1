package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class BlackDye extends Item {

    public BlackDye() {
        super("Black Dye", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A vial of black dye");
    }

    @Override
    public Item copy() {
        return new BlackDye();
    }
}
