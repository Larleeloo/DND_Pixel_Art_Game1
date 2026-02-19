package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

public class SilverEmeraldBracelet extends Item {

    public SilverEmeraldBracelet() {
        super("Silver Emerald Bracelet", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.RARE);
        setDescription("A silver bracelet set with a emerald");
    }

    @Override
    public Item copy() {
        return new SilverEmeraldBracelet();
    }
}
