package com.ambermoongame.entity.item.items.weapons.melee;

import com.ambermoongame.entity.item.Item;

/**
 * Battle Axe - Slow but powerful.
 * Uncommon melee weapon with high damage and slow attack speed.
 */
public class BattleAxe extends Item {

    public BattleAxe() {
        super("Battle Axe", ItemCategory.WEAPON);
        setDamage(25);
        setAttackSpeed(0.7f);
        setRange(70);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Slow but powerful");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new BattleAxe();
    }
}
