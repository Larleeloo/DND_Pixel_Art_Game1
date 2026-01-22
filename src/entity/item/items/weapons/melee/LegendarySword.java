package entity.item.items.weapons.melee;

import entity.item.Item;

/**
 * Excalibur - The sword of kings.
 * Legendary melee weapon with high damage and increased crit chance.
 */
public class LegendarySword extends Item {

    public LegendarySword() {
        super("Excalibur", ItemCategory.WEAPON);
        setDamage(35);
        setAttackSpeed(1.2f);
        setRange(80);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("The sword of kings");
        setCritChance(0.15f);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new LegendarySword();
    }
}
