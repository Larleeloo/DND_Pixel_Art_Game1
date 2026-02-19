package com.ambermoon.lootgame.entity.items.keys;

import com.ambermoon.lootgame.entity.Item;

public class LithosGodKey extends Item {

    public LithosGodKey() {
        super("Lithos God Key", ItemCategory.KEY);
        setRarity(ItemRarity.MYTHIC);
        setDescription("The divine key of Lithos, God of Earth");
        setSpecialEffect("Opens the gates of the earth realm");
        setWisdomRequirement(10);
    }

    @Override
    public Item copy() {
        return new LithosGodKey();
    }
}
