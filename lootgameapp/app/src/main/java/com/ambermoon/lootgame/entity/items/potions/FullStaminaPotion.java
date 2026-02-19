package com.ambermoon.lootgame.entity.items.potions;

import com.ambermoon.lootgame.entity.Item;

public class FullStaminaPotion extends Item {

    public FullStaminaPotion() {
        super("Full Stamina Potion", ItemCategory.POTION);
        setRarity(ItemRarity.RARE);
        setDescription("Fully restores stamina");
        setStaminaRestore(100);
        setConsumeTime(0.5f);
    }

    @Override
    public Item copy() {
        return new FullStaminaPotion();
    }
}
