package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class SilverSapphireNecklace extends Item {

    public SilverSapphireNecklace() {
        super("Silver Sapphire Necklace", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.RARE);
        setDescription("A silver necklace set with a sapphire");
    }

    @Override
    public Item copy() {
        return new SilverSapphireNecklace();
    }
}
