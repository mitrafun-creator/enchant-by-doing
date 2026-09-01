package aiefu.ebd.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow public int leftHeight;
    @Shadow public int rightHeight;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/gui/GuiLayerManager;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void adjustHudHeights(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        this.leftHeight = 33;
        this.rightHeight = 33;
    }
}
