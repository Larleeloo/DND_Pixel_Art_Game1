package com.ambermoon.lootgame.entity.items.ammo;

import com.ambermoon.lootgame.entity.Item;

public class CannonBall extends Item {

    public CannonBall() {
        super("Cannon Ball", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A heavy iron cannon ball");
    }

    @Override
    public Item copy() {
        return new CannonBall();
    }
}
