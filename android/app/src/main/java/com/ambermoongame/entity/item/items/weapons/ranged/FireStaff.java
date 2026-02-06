package com.ambermoongame.entity.item.items.weapons.ranged;

import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.ProjectileEntity.ProjectileType;

/**
 * Staff of Fire - Launches explosive fireballs.
 * Rare ranged weapon with explosion on impact and chargeable shots.
 */
public class FireStaff extends Item {

    public FireStaff() {
        super("Staff of Fire", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileType.FIREBALL, 25, 12.0f);
        setRarity(ItemRarity.RARE);
        setDescription("Launches explosive fireballs");
        setSpecialEffect("Explosion on impact");
        setAmmoItemName("mana");
        setChargeable(true, 3.0f, 30, 3.0f);
        setChargeSizeMultiplier(2.5f);
        setScalesWithIntelligence(true);
    }

    @Override
    public Item copy() {
        return new FireStaff();
    }
}
