package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class SilverSapphireBracelet extends Item {

    public SilverSapphireBracelet() {
        super("Silver Sapphire Bracelet", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.RARE);
        setDescription("A silver bracelet set with a sapphire");
    }

    @Override
    public Item copy() {
        return new SilverSapphireBracelet();
    }
}
