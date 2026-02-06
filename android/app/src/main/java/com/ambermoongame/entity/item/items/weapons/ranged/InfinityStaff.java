package com.ambermoongame.entity.item.items.weapons.ranged;

import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.ProjectileEntity.ProjectileType;

/**
 * Staff of Infinity - Contains the power of infinity.
 * Mythic ranged weapon with reality-bending damage. Requires very high Wisdom.
 */
public class InfinityStaff extends Item {

    public InfinityStaff() {
        super("Staff of Infinity", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileType.MAGIC_BOLT, 55, 20.0f);
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
