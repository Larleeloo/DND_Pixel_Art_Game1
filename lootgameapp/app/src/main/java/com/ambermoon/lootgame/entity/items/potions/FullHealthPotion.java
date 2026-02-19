package com.ambermoon.lootgame.entity.items.potions;

import com.ambermoon.lootgame.entity.Item;

public class FullHealthPotion extends Item {

    public FullHealthPotion() {
        super("Full Health Potion", ItemCategory.POTION);
        setRarity(ItemRarity.RARE);
        setDescription("Fully restores health");
        setHealthRestore(100);
        setConsumeTime(0.5f);
    }

    @Override
    public Item copy() {
        return new FullHealthPotion();
    }
}
