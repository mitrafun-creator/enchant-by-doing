package aiefu.ebd.mixin;

import aiefu.ebd.EBDCommon;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrushItem.class)
public class BrushItemMixin {
    @Inject(method = "onUseTick", at = @At("HEAD"), cancellable = true)
    private void onUseTickEOVR(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration, CallbackInfo ci) {
        if (livingEntity instanceof Player player) {
            InteractionHand hand = player.getUsedItemHand();
            if (hand != null) {
                ItemStack offhand = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
                if (offhand.is(EBDCommon.DUSTY_BOOK.get())) {
                    ci.cancel();
                }
            }
        }
    }
}
