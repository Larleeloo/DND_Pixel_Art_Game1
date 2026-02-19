package com.ambermoon.lootgame.entity.items.potions;

import com.ambermoon.lootgame.entity.Item;

public class BlankPotion extends Item {

    public BlankPotion() {
        super("Blank Potion", ItemCategory.POTION);
        setRarity(ItemRarity.COMMON);
        setDescription("An empty potion bottle with no enchantment");
    }

    @Override
    public Item copy() {
        return new BlankPotion();
    }
}
