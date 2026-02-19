package com.ambermoon.lootgame.entity.items.ammo;

import com.ambermoon.lootgame.entity.Item;

public class PoisonArrow extends Item {

    public PoisonArrow() {
        super("Poison Arrow", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("An arrow tipped with poison");
    }

    @Override
    public Item copy() {
        return new PoisonArrow();
    }
}
