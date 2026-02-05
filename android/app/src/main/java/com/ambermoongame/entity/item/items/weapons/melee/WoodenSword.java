package com.ambermoongame.entity.item.items.weapons.melee;

import com.ambermoongame.entity.item.Item;

/**
 * Wooden Sword - A basic training sword.
 * Common melee weapon with low damage but fast attack speed.
 */
public class WoodenSword extends Item {

    public WoodenSword() {
        super("Wooden Sword", ItemCategory.WEAPON);
        setDamage(5);
        setAttackSpeed(1.2f);
        setRange(50);
        setRarity(ItemRarity.COMMON);
        setDescription("A basic training sword");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new WoodenSword();
    }
}
