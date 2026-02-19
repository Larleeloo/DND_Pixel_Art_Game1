package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Feather extends Item {

    public Feather() {
        super("Feather", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A soft feather");
    }

    @Override
    public Item copy() {
        return new Feather();
    }
}
