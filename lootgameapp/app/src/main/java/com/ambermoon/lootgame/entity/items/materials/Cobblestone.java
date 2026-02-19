package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Cobblestone extends Item {

    public Cobblestone() {
        super("Cobblestone", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A rough cobblestone block");
    }

    @Override
    public Item copy() {
        return new Cobblestone();
    }
}
