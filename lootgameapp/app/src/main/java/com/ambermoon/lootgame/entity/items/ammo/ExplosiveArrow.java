package com.ambermoon.lootgame.entity.items.ammo;

import com.ambermoon.lootgame.entity.Item;

public class ExplosiveArrow extends Item {

    public ExplosiveArrow() {
        super("Explosive Arrow", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("An arrow rigged to explode on impact");
    }

    @Override
    public Item copy() {
        return new ExplosiveArrow();
    }
}
