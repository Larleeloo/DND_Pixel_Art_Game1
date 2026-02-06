package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

/**
 * Void Armor - Woven from the fabric of space.
 * Legendary armor with damage reduction and evasion. Ancient artifact.
 */
public class VoidArmor extends Item {

    public VoidArmor() {
        super("Void Armor", ItemCategory.ARMOR);
        setDefense(35);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Woven from the fabric of space");
        setSpecialEffect("+30% damage reduction, +15% evasion");
        setWisdomRequirement(7);
    }

    @Override
    public Item copy() {
        return new VoidArmor();
    }
}
