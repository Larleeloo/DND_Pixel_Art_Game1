package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class BlueDye extends Item {

    public BlueDye() {
        super("Blue Dye", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A vial of blue dye");
    }

    @Override
    public Item copy() {
        return new BlueDye();
    }
}
