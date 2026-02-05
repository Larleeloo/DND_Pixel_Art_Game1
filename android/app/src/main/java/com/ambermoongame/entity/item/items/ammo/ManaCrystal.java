package com.ambermoongame.entity.item.items.ammo;

import com.ambermoongame.entity.item.Item;

/**
 * Mana Crystal - Powers magic weapons.
 * Uncommon stackable material for magic ammunition.
 */
public class ManaCrystal extends Item {

    public ManaCrystal() {
        super("Mana Crystal", ItemCategory.MATERIAL);
        setDamage(0);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Powers magic weapons");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new ManaCrystal();
    }
}
