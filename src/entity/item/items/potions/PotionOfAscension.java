package entity.item.items.potions;

import entity.item.Item;

/**
 * Potion of Ascension - Transcend mortal limits.
 * Legendary potion that fully restores and doubles all stats.
 */
public class PotionOfAscension extends Item {

    public PotionOfAscension() {
        super("Potion of Ascension", ItemCategory.POTION);
        setHealthRestore(100);
        setManaRestore(100);
        setStaminaRestore(100);
        setConsumeTime(0.5f);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Transcend mortal limits");
        setSpecialEffect("Double all stats for 60 seconds");
    }

    @Override
    public Item copy() {
        return new PotionOfAscension();
    }
}
