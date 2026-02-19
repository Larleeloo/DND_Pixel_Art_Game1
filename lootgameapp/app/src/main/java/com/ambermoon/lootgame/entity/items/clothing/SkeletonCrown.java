package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class SkeletonCrown extends Item {

    public SkeletonCrown() {
        super("Skeleton Crown", ItemCategory.CLOTHING);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A crown fashioned from bones");
        setSpecialEffect("Command undead");
    }

    @Override
    public Item copy() {
        return new SkeletonCrown();
    }
}
