package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class ArchmageRobes extends Item {

    public ArchmageRobes() {
        super("Archmage Robes", ItemCategory.ARMOR);
        setRarity(ItemRarity.EPIC);
        setDescription("Robes of a master wizard");
        setDefense(15);
        setSpecialEffect("+30% magic damage");
        setScalesWithIntelligence(true);
    }

    @Override
    public Item copy() {
        return new ArchmageRobes();
    }
}
