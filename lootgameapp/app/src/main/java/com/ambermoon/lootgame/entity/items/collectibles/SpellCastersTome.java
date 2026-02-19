package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

public class SpellCastersTome extends Item {

    public SpellCastersTome() {
        super("Spell Caster's Tome", ItemCategory.OTHER);
        setRarity(ItemRarity.EPIC);
        setDescription("An ancient tome of powerful spells");
    }

    @Override
    public Item copy() {
        return new SpellCastersTome();
    }
}
