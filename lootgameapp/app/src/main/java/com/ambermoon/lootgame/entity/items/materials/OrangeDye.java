package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class OrangeDye extends Item {

    public OrangeDye() {
        super("Orange Dye", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A vial of orange dye");
    }

    @Override
    public Item copy() {
        return new OrangeDye();
    }
}
