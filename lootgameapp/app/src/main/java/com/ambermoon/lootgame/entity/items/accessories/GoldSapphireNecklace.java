package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class GoldSapphireNecklace extends Item {

    public GoldSapphireNecklace() {
        super("Gold Sapphire Necklace", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.EPIC);
        setDescription("A gold necklace set with a sapphire");
    }

    @Override
    public Item copy() {
        return new GoldSapphireNecklace();
    }
}
