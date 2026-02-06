package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ProjectileEntity;

/**
 * Staff of Infinity - Contains the power of infinity.
 * Mythic ranged weapon with reality-bending damage. Requires very high Wisdom.
 */
public class InfinityStaff extends Item {

    public InfinityStaff() {
        super("Staff of Infinity", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_MAGIC_BOLT, 55, 20.0f);
        setRarity(ItemRarity.MYTHIC);
        setDescription("Contains the power of infinity");
        setSpecialEffect("Reality-bending damage");
        setAmmoItemName("mana");
        setChargeable(true, 5.0f, 50, 5.0f);
        setChargeSizeMultiplier(4.0f);
        setWisdomRequirement(9);
        setScalesWithIntelligence(true);
    }

    @Override
    public Item copy() {
        return new InfinityStaff();
    }
}
