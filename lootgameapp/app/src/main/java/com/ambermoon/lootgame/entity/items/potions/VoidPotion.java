package com.ambermoon.lootgame.entity.items.potions;

import com.ambermoon.lootgame.entity.Item;

public class VoidPotion extends Item {

    public VoidPotion() {
        super("Void Potion", ItemCategory.POTION);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A potion brewed from void essence");
        setConsumeTime(0.5f);
        setSpecialEffect("Phase through matter briefly");
    }

    @Override
    public Item copy() {
        return new VoidPotion();
    }
}
