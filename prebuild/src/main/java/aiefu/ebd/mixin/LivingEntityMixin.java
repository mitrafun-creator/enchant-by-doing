package aiefu.ebd.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @org.spongepowered.asm.mixin.Shadow public boolean swinging;
    @org.spongepowered.asm.mixin.Shadow public int swingTime;
    @org.spongepowered.asm.mixin.Shadow protected abstract int getCurrentSwingDuration();

    @Unique
    private boolean ebd$alternateSwing = false;

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    private void onSwing(InteractionHand hand, boolean updateOutline, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player) {
            if (aiefu.ebd.Utils.getSkillLevel(player, "lumberjack") >= 80) {
                net.minecraft.world.item.ItemStack main = player.getMainHandItem();
                net.minecraft.world.item.ItemStack off = player.getOffhandItem();
                if (main.getItem() instanceof net.minecraft.world.item.AxeItem && off.getItem() instanceof net.minecraft.world.item.AxeItem) {
                    if (hand == InteractionHand.MAIN_HAND) {
                        if (!this.swinging || this.swingTime >= this.getCurrentSwingDuration() / 2 || this.swingTime < 0) {
                            if (ebd$alternateSwing) {
                                ebd$alternateSwing = false;
                                player.swing(InteractionHand.OFF_HAND, updateOutline);
                                ci.cancel();
                            } else {
                                ebd$alternateSwing = true;
                            }
                        }
                    }
                }
            }
        }
    }
}
