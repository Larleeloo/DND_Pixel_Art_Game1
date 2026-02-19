package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class VoidStone extends Item {

    public VoidStone() {
        super("Void Stone", ItemCategory.MATERIAL);
        setRarity(ItemRarity.EPIC);
        setDescription("A stone pulsing with void energy");
    }

    @Override
    public Item copy() {
        return new VoidStone();
    }
}
