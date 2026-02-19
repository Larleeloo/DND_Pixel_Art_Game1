package com.ambermoon.lootgame.entity.items.food;

import com.ambermoon.lootgame.entity.Item;

public class BerriesOfTheOldGods extends Item {

    public BerriesOfTheOldGods() {
        super("Berries of the Old Gods", ItemCategory.FOOD);
        setRarity(ItemRarity.EPIC);
        setDescription("Ancient berries of divine origin");
        setHealthRestore(100);
        setManaRestore(100);
        setStaminaRestore(100);
        setConsumeTime(2.0f);
        setSpecialEffect("Blessing of the Old Gods");
    }

    @Override
    public Item copy() {
        return new BerriesOfTheOldGods();
    }
}
