package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class ChainmailPants extends Item {

    public ChainmailPants() {
        super("Chainmail Pants", ItemCategory.ARMOR);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Protective chainmail leggings");
        setDefense(6);
    }

    @Override
    public Item copy() {
        return new ChainmailPants();
    }
}
