package com.ambermoon.lootgame.entity.items.ammo;

import com.ambermoon.lootgame.entity.Item;

public class ExplosiveBolt extends Item {

    public ExplosiveBolt() {
        super("Explosive Bolt", ItemCategory.OTHER);
        setRarity(ItemRarity.EPIC);
        setDescription("A crossbow bolt rigged to explode");
    }

    @Override
    public Item copy() {
        return new ExplosiveBolt();
    }
}
