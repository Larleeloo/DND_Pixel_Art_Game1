package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class GoldSapphireBracelet extends Item {

    public GoldSapphireBracelet() {
        super("Gold Sapphire Bracelet", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.EPIC);
        setDescription("A gold bracelet set with a sapphire");
    }

    @Override
    public Item copy() {
        return new GoldSapphireBracelet();
    }
}
