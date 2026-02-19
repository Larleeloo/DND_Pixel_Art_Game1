package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class ScrollOfIceCrystal extends Item {

    public ScrollOfIceCrystal() {
        super("Scroll of Ice Crystal", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("A scroll inscribed with an ice crystal spell");
    }

    @Override
    public Item copy() {
        return new ScrollOfIceCrystal();
    }
}
