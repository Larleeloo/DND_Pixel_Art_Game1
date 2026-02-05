package com.ambermoongame.entity.item.items.food;

import com.ambermoongame.entity.item.Item;

/**
 * Golden Apple - A magical fruit.
 * Rare food item that restores health, mana, and grants temporary invincibility.
 */
public class GoldenApple extends Item {

    public GoldenApple() {
        super("Golden Apple", ItemCategory.FOOD);
        setHealthRestore(50);
        setManaRestore(20);
        setStaminaRestore(30);
        setConsumeTime(1.5f);
        setRarity(ItemRarity.RARE);
        setDescription("A magical fruit");
        setSpecialEffect("Temporary invincibility");
    }

    @Override
    public Item copy() {
        return new GoldenApple();
    }
}
