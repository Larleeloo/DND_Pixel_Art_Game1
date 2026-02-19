package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class SilverEmeraldNecklace extends Item {

    public SilverEmeraldNecklace() {
        super("Silver Emerald Necklace", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.RARE);
        setDescription("A silver necklace set with a emerald");
    }

    @Override
    public Item copy() {
        return new SilverEmeraldNecklace();
    }
}
