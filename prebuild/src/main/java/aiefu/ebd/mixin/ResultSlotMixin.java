package aiefu.ebd.mixin;

import aiefu.ebd.workstation.WorkstationSoundHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {

    @Inject(method = "onTake", at = @At("TAIL"))
    private void onTakeCraftResult(Player player, ItemStack stack, CallbackInfo ci) {
        if (!player.level().isClientSide() && player instanceof ServerPlayer sp && !stack.isEmpty()) {
            WorkstationSoundHelper.playCraftingSounds(sp, stack);
        }
    }
}
