package aiefu.ebd.mixin;

import aiefu.ebd.LBDConfig;
import aiefu.ebd.workstation.WorkstationHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Shadow public abstract ItemStack getItem();

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void onSlotMayPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ResultSlot)) return;

        ItemStack stack = this.getItem();
        if (stack.isEmpty()) return;

        if (aiefu.ebd.GlobalPerks.isItemLockedForPlayer(player, stack)) {
            cir.setReturnValue(false);
            return;
        }

        if (!LBDConfig.INSTANCE.enableCraftingWorkstations) return;

        byte reqMask = WorkstationHelper.getRequiredWorkstationsMask(stack);
        if (reqMask != 0) {
            byte nearbyMask = WorkstationHelper.getNearbyWorkstationsMask(
                    player.level(),
                    player.blockPosition(),
                    LBDConfig.INSTANCE.workstationDetectionRadius
            );
            if ((reqMask & ~nearbyMask) != 0) {
                cir.setReturnValue(false);
            }
        }
    }
}
