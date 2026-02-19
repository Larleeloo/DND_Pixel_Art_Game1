package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class LeatherTunic extends Item {

    public LeatherTunic() {
        super("Leather Tunic", ItemCategory.ARMOR);
        setRarity(ItemRarity.COMMON);
        setDescription("A basic leather tunic");
        setDefense(3);
    }

    @Override
    public Item copy() {
        return new LeatherTunic();
    }
}
