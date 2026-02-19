package com.ambermoon.lootgame.entity.items.keys;

import com.ambermoon.lootgame.entity.Item;

public class OpalKey extends Item {

    public OpalKey() {
        super("Opal Key", ItemCategory.KEY);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A key carved from a single opal");
    }

    @Override
    public Item copy() {
        return new OpalKey();
    }
}
