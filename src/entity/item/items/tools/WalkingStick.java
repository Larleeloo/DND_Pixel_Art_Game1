package entity.item.items.tools;

import entity.item.Item;

/**
 * Walking Stick - Helps with long journeys.
 * Common tool with movement speed bonus.
 */
public class WalkingStick extends Item {

    public WalkingStick() {
        super("Walking Stick", ItemCategory.TOOL);
        setDamage(3);
        setRarity(ItemRarity.COMMON);
        setDescription("Helps with long journeys");
        setSpecialEffect("+5% movement speed");
    }

    @Override
    public Item copy() {
        return new WalkingStick();
    }
}
