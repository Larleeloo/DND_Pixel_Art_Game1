package com.ambermoon.lootgame.entity.items.food;

import com.ambermoon.lootgame.entity.Item;

public class MagicApple extends Item {

    public MagicApple() {
        super("Magic Apple", ItemCategory.FOOD);
        setRarity(ItemRarity.EPIC);
        setDescription("An apple infused with magical energy");
        setHealthRestore(75);
        setManaRestore(50);
        setConsumeTime(1.5f);
        setSpecialEffect("Temporary magic boost");
    }

    @Override
    public Item copy() {
        return new MagicApple();
    }
}
