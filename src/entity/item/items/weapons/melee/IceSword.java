package entity.item.items.weapons.melee;

import entity.item.Item;

/**
 * Frost Edge - Slows enemies on hit.
 * Rare melee weapon with slow effect.
 */
public class IceSword extends Item {

    public IceSword() {
        super("Frost Edge", ItemCategory.WEAPON);
        setDamage(18);
        setAttackSpeed(0.9f);
        setRange(65);
        setRarity(ItemRarity.RARE);
        setDescription("Slows enemies on hit");
        setSpecialEffect("Slow effect for 2 seconds");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new IceSword();
    }
}
