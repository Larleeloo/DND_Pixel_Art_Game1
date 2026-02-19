package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class PurpleDye extends Item {

    public PurpleDye() {
        super("Purple Dye", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A vial of purple dye");
    }

    @Override
    public Item copy() {
        return new PurpleDye();
    }
}
