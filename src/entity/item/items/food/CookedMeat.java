package entity.item.items.food;

import entity.item.Item;

/**
 * Cooked Meat - A filling meal.
 * Common food item that restores significant health.
 */
public class CookedMeat extends Item {

    public CookedMeat() {
        super("Cooked Meat", ItemCategory.FOOD);
        setHealthRestore(40);
        setManaRestore(0);
        setStaminaRestore(20);
        setConsumeTime(1.5f);
        setRarity(ItemRarity.COMMON);
        setDescription("A filling meal");
    }

    @Override
    public Item copy() {
        return new CookedMeat();
    }
}
