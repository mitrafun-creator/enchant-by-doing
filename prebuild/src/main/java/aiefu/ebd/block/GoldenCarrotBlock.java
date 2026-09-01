package aiefu.ebd.block;

import net.minecraft.world.level.block.CarrotBlock;

/**
 * Golden Carrot Crop block. Behaves exactly like vanilla Carrots
 * but drops the custom "Gilded Carrot" item (enchant_by_doing:golden_carrot)
 * via its own loot table.
 * Only plantable at Farming level 100 (enforced in EBDGameplayEvents.onRightClickBlock).
 */
public class GoldenCarrotBlock extends CarrotBlock {

    public GoldenCarrotBlock(Properties properties) {
        super(properties);
    }
}
