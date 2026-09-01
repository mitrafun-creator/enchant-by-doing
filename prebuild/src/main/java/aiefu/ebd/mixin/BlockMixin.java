package aiefu.ebd.mixin;

import aiefu.ebd.IBlockEntityEnchanted;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private static void ebd$modifyDrops(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos,
                                        BlockEntity blockEntity, Entity entity, ItemStack tool,
                                        CallbackInfoReturnable<List<ItemStack>> cir) {
        if (blockEntity instanceof IBlockEntityEnchanted enchanted) {
            ItemEnchantments enc = enchanted.ebd$getBlockEntityEnchantments();
            if (enc != null && !enc.isEmpty()) {
                List<ItemStack> drops = cir.getReturnValue();
                if (drops != null) {
                    for (ItemStack drop : drops) {
                        if (drop.getItem() instanceof net.minecraft.world.item.BlockItem blockItem && blockItem.getBlock() == state.getBlock()) {
                            drop.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, enc);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "getCloneItemStack", at = @At("RETURN"), cancellable = true)
    private void ebd$modifyCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state,
                                          CallbackInfoReturnable<ItemStack> cir) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IBlockEntityEnchanted enchanted) {
            ItemEnchantments enc = enchanted.ebd$getBlockEntityEnchantments();
            if (enc != null && !enc.isEmpty()) {
                ItemStack stack = cir.getReturnValue();
                if (stack != null && !stack.isEmpty()) {
                    stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, enc);
                }
            }
        }
    }
}
