package aiefu.ebd.mixin;

import aiefu.ebd.client.gui.WorkstationGuiRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {

    public InventoryScreenMixin(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderWorkstationBadges(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        int startX = this.leftPos + this.imageWidth + 4;
        if (startX + 24 > this.width) {
            startX = this.leftPos + this.imageWidth - 24;
        }
        int startY = this.topPos + 10;
        WorkstationGuiRenderer.renderBadges(graphics, this.font, startX, startY, mouseX, mouseY);
    }
}