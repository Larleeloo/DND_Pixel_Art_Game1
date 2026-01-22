package entity.item.items.potions;

import entity.item.Item;

/**
 * Stamina Potion - Restores all stamina.
 * Common potion for stamina recovery.
 */
public class StaminaPotion extends Item {

    public StaminaPotion() {
        super("Stamina Potion", ItemCategory.POTION);
        setHealthRestore(0);
        setManaRestore(0);
        setStaminaRestore(100);
        setConsumeTime(0.5f);
        setRarity(ItemRarity.COMMON);
        setDescription("Restores all stamina");
    }

    @Override
    public Item copy() {
        return new StaminaPotion();
    }
}
