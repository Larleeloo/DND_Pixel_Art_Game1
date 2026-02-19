package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Poison extends Item {

    public Poison() {
        super("Poison", ItemCategory.MATERIAL);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A vial of deadly poison");
    }

    @Override
    public Item copy() {
        return new Poison();
    }
}
