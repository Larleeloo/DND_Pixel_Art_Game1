package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

public class UndyingStone extends Item {

    public UndyingStone() {
        super("Undying Stone", ItemCategory.OTHER);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A stone that defies death itself");
        setSpecialEffect("Prevents death once per day");
    }

    @Override
    public Item copy() {
        return new UndyingStone();
    }
}
