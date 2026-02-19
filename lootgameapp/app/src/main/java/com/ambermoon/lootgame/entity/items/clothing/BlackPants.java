package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class BlackPants extends Item {

    public BlackPants() {
        super("Black Pants", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of black pants");
    }

    @Override
    public Item copy() {
        return new BlackPants();
    }
}
