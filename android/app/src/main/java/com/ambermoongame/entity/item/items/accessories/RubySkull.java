package com.ambermoongame.entity.item.items.accessories;

import com.ambermoongame.entity.item.Item;

/**
 * The Ruby Skull - A crimson skull pulsing with dark energy.
 * Legendary accessory that grants unlimited jumps while held.
 */
public class RubySkull extends Item {

    public RubySkull() {
        super("The Ruby Skull", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A crimson skull pulsing with dark energy");
        setSpecialEffect("Unlimited jumps while held");
        setStackable(false);
        setMaxStackSize(1);
    }

    @Override
    public Item copy() {
        return new RubySkull();
    }
}
