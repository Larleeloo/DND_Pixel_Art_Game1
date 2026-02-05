package com.ambermoongame.entity.item.items.clothing;

import com.ambermoongame.entity.item.Item;

/**
 * Gown of Forgotten Verses - Whispers ancient incantations.
 * Epic clothing with mana regeneration bonus.
 */
public class GownForgottenVerses extends Item {

    public GownForgottenVerses() {
        super("Gown of Forgotten Verses", ItemCategory.CLOTHING);
        setRarity(ItemRarity.EPIC);
        setDescription("Whispers ancient incantations");
        setSpecialEffect("+20% mana regeneration");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new GownForgottenVerses();
    }
}
