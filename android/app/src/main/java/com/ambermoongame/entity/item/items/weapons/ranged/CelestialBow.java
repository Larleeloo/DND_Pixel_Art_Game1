package com.ambermoongame.entity.item.items.weapons.ranged;

import com.ambermoongame.entity.item.Item;
import entity.ProjectileEntity.ProjectileType;

/**
 * Celestial Bow - Fires arrows of pure starlight.
 * Mythic ranged weapon with extreme power. Requires high Wisdom.
 */
public class CelestialBow extends Item {

    public CelestialBow() {
        super("Celestial Bow", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileType.ARROW, 45, 30.0f);
        setRarity(ItemRarity.MYTHIC);
        setDescription("Fires arrows of pure starlight");
        setAmmoItemName("arrow");
        setChargeable(true, 1.5f, 0, 4.0f);
        setChargeSpeedMultiplier(3.0f);
        setCritChance(0.25f);
        setWisdomRequirement(8);
        setScalesWithDexterity(true);
    }

    @Override
    public Item copy() {
        return new CelestialBow();
    }
}
