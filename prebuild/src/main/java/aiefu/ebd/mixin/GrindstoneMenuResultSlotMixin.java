package aiefu.ebd.mixin;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.IGrindstoneMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
public class GrindstoneMenuResultSlotMixin {
    @Shadow(aliases = "this$0")
    @Final
    private GrindstoneMenu this$0;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void onTake(Player player, ItemStack stack, CallbackInfo ci) {
        if (player.level().isClientSide()) return;
        if (player instanceof ServerPlayer serverPlayer) {
            Container repairSlots = ((IGrindstoneMenu) this.this$0).ebd$getRepairSlots();
            ItemStack input1 = repairSlots.getItem(0);
            ItemStack input2 = repairSlots.getItem(1);
            EBDCommon.handleGrindstoneDisenchant(serverPlayer, input1, input2);
        }
    }
}
