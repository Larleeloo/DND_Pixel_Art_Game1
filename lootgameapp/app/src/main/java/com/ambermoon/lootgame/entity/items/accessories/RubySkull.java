package com.ambermoon.lootgame.entity.items.accessories;

import com.ambermoon.lootgame.entity.Item;

/**
 * The Ruby Skull - A crimson skull pulsing with dark energy.
 * Legendary accessory that grants unlimited jumps while held.
 */
public class RubySkull extends Item {

    public RubySkull() {
        super("The Ruby Skull", ItemCategory.ACCESSORY);
        setRarity(ItemRarity.EPIC);
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
