package com.ambermoon.lootgame.entity.items.tools;

import com.ambermoon.lootgame.entity.Item;

public class DiamondEngravedPickaxe extends Item {

    public DiamondEngravedPickaxe() {
        super("Diamond Engraved Pickaxe", ItemCategory.TOOL);
        setRarity(ItemRarity.EPIC);
        setDescription("A pickaxe with diamond-tipped edges");
        setDamage(8);
        setAttackSpeed(1.0f);
    }

    @Override
    public Item copy() {
        return new DiamondEngravedPickaxe();
    }
}
