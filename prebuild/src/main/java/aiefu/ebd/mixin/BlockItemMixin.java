package aiefu.ebd.mixin;

import aiefu.ebd.IBlockEntityEnchanted;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(method = "updateCustomBlockEntityTag(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("TAIL"))
    private static void ebd$updateCustomBlockEntityTag(Level level, Player player, BlockPos pos, ItemStack stack,
                                                      CallbackInfoReturnable<Boolean> cir) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IBlockEntityEnchanted enchantedBe) {
            ItemEnchantments enc = stack.getEnchantments();
            if (enc != null && !enc.isEmpty()) {
                enchantedBe.ebd$setBlockEntityEnchantments(enc);
                be.setChanged();
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            }
        }
    }
}
