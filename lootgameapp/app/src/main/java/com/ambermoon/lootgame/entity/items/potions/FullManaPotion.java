package com.ambermoon.lootgame.entity.items.potions;

import com.ambermoon.lootgame.entity.Item;

public class FullManaPotion extends Item {

    public FullManaPotion() {
        super("Full Mana Potion", ItemCategory.POTION);
        setRarity(ItemRarity.RARE);
        setDescription("Fully restores mana");
        setManaRestore(100);
        setConsumeTime(0.5f);
    }

    @Override
    public Item copy() {
        return new FullManaPotion();
    }
}
