package com.ambermoon.lootgame.entity.items.potions;

import com.ambermoon.lootgame.entity.Item;

public class PurplePotion extends Item {

    public PurplePotion() {
        super("Purple Potion", ItemCategory.POTION);
        setRarity(ItemRarity.RARE);
        setDescription("A mysterious purple potion");
        setConsumeTime(0.5f);
        setSpecialEffect("Unknown magical effect");
    }

    @Override
    public Item copy() {
        return new PurplePotion();
    }
}
