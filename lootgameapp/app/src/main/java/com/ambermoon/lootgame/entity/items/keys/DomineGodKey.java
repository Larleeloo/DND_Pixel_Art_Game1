package com.ambermoon.lootgame.entity.items.keys;

import com.ambermoon.lootgame.entity.Item;

public class DomineGodKey extends Item {

    public DomineGodKey() {
        super("Domine God Key", ItemCategory.KEY);
        setRarity(ItemRarity.MYTHIC);
        setDescription("The divine key of Domine, God of Order");
        setSpecialEffect("Opens the gates of the order realm");
        setWisdomRequirement(10);
    }

    @Override
    public Item copy() {
        return new DomineGodKey();
    }
}
