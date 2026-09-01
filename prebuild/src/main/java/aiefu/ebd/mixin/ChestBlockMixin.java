package aiefu.ebd.mixin;

import aiefu.ebd.IBlockEntityEnchanted;
import aiefu.ebd.Utils;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public abstract class ChestBlockMixin {

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void ebd$preventDoubleChestPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        if (state == null) return;
        ItemStack stack = context.getItemInHand();
        if (Utils.getEnchantmentLevel(stack.getEnchantments(), "capacity") > 0) {
            cir.setReturnValue(state.setValue(ChestBlock.TYPE, net.minecraft.world.level.block.state.properties.ChestType.SINGLE));
        }
    }

    @Inject(method = "candidatePartnerFacing", at = @At("HEAD"), cancellable = true)
    private void ebd$preventEnchantedPartner(BlockPlaceContext context, Direction direction, CallbackInfoReturnable<Direction> cir) {
        ItemStack stack = context.getItemInHand();
        if (Utils.getEnchantmentLevel(stack.getEnchantments(), "capacity") > 0) {
            cir.setReturnValue(null);
            return;
        }
        net.minecraft.core.BlockPos neighborPos = context.getClickedPos().relative(direction);
        BlockEntity be = context.getLevel().getBlockEntity(neighborPos);
        if (be instanceof IBlockEntityEnchanted enchanted && Utils.getEnchantmentLevel(enchanted.ebd$getBlockEntityEnchantments(), "capacity") > 0) {
            cir.setReturnValue(null);
        }
    }
}
