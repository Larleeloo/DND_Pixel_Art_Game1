package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class RedDye extends Item {

    public RedDye() {
        super("Red Dye", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A vial of red dye");
    }

    @Override
    public Item copy() {
        return new RedDye();
    }
}
