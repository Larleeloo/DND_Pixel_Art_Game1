package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class WhiteDye extends Item {

    public WhiteDye() {
        super("White Dye", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A vial of white dye");
    }

    @Override
    public Item copy() {
        return new WhiteDye();
    }
}
