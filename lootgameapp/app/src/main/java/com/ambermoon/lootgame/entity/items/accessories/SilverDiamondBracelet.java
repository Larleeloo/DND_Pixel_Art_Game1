package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class SilverDiamondBracelet extends Item {

    public SilverDiamondBracelet() {
        super("Silver Diamond Bracelet", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.RARE);
        setDescription("A silver bracelet set with a diamond");
    }

    @Override
    public Item copy() {
        return new SilverDiamondBracelet();
    }
}
