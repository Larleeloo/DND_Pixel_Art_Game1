package com.ambermoongame.entity.item.items.clothing;

import com.ambermoongame.entity.item.Item;

/**
 * Chameleon Cloak - Changes color to blend in.
 * Rare clothing with partial invisibility effect.
 */
public class ChameleonCloak extends Item {

    public ChameleonCloak() {
        super("Chameleon Cloak", ItemCategory.CLOTHING);
        setRarity(ItemRarity.RARE);
        setDescription("Changes color to blend in");
        setSpecialEffect("Partial invisibility");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new ChameleonCloak();
    }
}
