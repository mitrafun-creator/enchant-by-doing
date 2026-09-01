package aiefu.ebd.item;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Gilded Carrot — edible food item that also acts as a seed for GoldenCarrotBlock.
 * Can only be planted at Farming level 100.
 */
public class GoldenCarrotItem extends Item {

    public GoldenCarrotItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockPos above = clickedPos.above();
        BlockState clickedState = level.getBlockState(clickedPos);
        BlockState aboveState = level.getBlockState(above);

        // Only act when clicking on farmland with air above
        if (!clickedState.is(Blocks.FARMLAND) || !aboveState.isAir()) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            int farmerLevel = Utils.getSkillLevel(player, "farmer");
            if (farmerLevel >= 100) {
                level.setBlockAndUpdate(above, EBDCommon.GOLDEN_CARROT_BLOCK.get().defaultBlockState());
                if (!player.getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
                level.playSound(null, above, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                player.sendSystemMessage(Component.literal(
                        "§6[ESO] §eТребуется уровень Фермерства 100 чтобы посадить Позолоченную Морковь! (Текущий: " + farmerLevel + ")"));
            }
        }

        // Always return success when clicking on farmland to prevent the default interaction
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
