package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;

public class StaffOf1000Souls extends Item {

    public StaffOf1000Souls() {
        super("Staff of 1000 Souls", ItemCategory.RANGED_WEAPON);
        setRarity(ItemRarity.MYTHIC);
        setDescription("A staff containing a thousand trapped souls");
        setDamage(45);
        setAttackSpeed(1.5f);
        setRange(100);
        setSpecialEffect("Drains souls on hit");
        setWisdomRequirement(9);
        setScalesWithIntelligence(true);
    }

    @Override
    public Item copy() {
        return new StaffOf1000Souls();
    }
}
