package com.ambermoon.lootgame.entity.items.keys;

import com.ambermoon.lootgame.entity.Item;

public class ManaGodKey extends Item {

    public ManaGodKey() {
        super("Mana God Key", ItemCategory.KEY);
        setRarity(ItemRarity.MYTHIC);
        setDescription("The divine key of the Mana God");
        setSpecialEffect("Opens the gates of the mana realm");
        setWisdomRequirement(10);
    }

    @Override
    public Item copy() {
        return new ManaGodKey();
    }
}
