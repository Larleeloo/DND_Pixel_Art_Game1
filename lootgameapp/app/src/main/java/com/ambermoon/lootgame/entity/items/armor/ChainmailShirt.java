package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class ChainmailShirt extends Item {

    public ChainmailShirt() {
        super("Chainmail Shirt", ItemCategory.ARMOR);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A protective chainmail shirt");
        setDefense(8);
    }

    @Override
    public Item copy() {
        return new ChainmailShirt();
    }
}
