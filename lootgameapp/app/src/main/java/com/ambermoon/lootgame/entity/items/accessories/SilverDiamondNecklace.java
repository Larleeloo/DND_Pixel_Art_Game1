package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class SilverDiamondNecklace extends Item {

    public SilverDiamondNecklace() {
        super("Silver Diamond Necklace", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.RARE);
        setDescription("A silver necklace set with a diamond");
    }

    @Override
    public Item copy() {
        return new SilverDiamondNecklace();
    }
}
