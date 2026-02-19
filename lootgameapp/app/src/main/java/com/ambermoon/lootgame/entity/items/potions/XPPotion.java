package com.ambermoon.lootgame.entity.items.potions;

import com.ambermoon.lootgame.entity.Item;

public class XPPotion extends Item {

    public XPPotion() {
        super("XP Potion", ItemCategory.POTION);
        setRarity(ItemRarity.RARE);
        setDescription("Grants a burst of experience");
        setConsumeTime(0.5f);
        setSpecialEffect("Grants bonus XP");
    }

    @Override
    public Item copy() {
        return new XPPotion();
    }
}
