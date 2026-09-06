package aiefu.ebd.mixin;

import aiefu.ebd.network.ClientsideNetworkManager;
import aiefu.ebd.workstation.WorkstationHelper;
import aiefu.ebd.workstation.WorkstationType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin extends AbstractContainerScreen<CraftingMenu> {

    public CraftingScreenMixin(CraftingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderWorkstationBadges(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        byte nearbyMask = ClientsideNetworkManager.clientNearbyWorkstationsMask;
        String missingId = ClientsideNetworkManager.clientMissingWorkstationId;

        int startX = this.leftPos + this.imageWidth + 4;
        if (startX + 24 > this.width) {
            startX = this.leftPos + this.imageWidth - 24;
        }
        int startY = this.topPos + 10;
        int badgeSize = 22;

        WorkstationType hoveredType = null;
        boolean hoveredPresent = false;
        boolean hoveredMissingAndReq = false;

        WorkstationType[] types = WorkstationType.values();
        for (int i = 0; i < types.length; i++) {
            WorkstationType type = types[i];
            int bx = startX;
            int by = startY + i * (badgeSize + 4);

            boolean isPresent = WorkstationHelper.isWorkstationPresent(nearbyMask, type);
            boolean isMissingAndReq = type.id.equalsIgnoreCase(missingId);

            // 1. Draw Background Box
            graphics.fill(bx, by, bx + badgeSize, by + badgeSize, 0xD0181820);

            // 2. Draw Borders and Effects
            if (isMissingAndReq) {
                // Pulsing Red Outline
                float pulse = (float) Math.sin(System.currentTimeMillis() * 0.008f);
                int alpha = (int) (180 + 75 * pulse);
                int redColor = (alpha << 24) | 0xFF2222;
                graphics.renderOutline(bx, by, badgeSize, badgeSize, redColor);
                graphics.renderOutline(bx - 1, by - 1, badgeSize + 2, badgeSize + 2, (alpha / 2 << 24) | 0xFF4444);
            } else if (isPresent) {
                // Vibrant Green Outline
                graphics.renderOutline(bx, by, badgeSize, badgeSize, 0xFF4CAF50);
            } else {
                // Muted Gray Outline
                graphics.renderOutline(bx, by, badgeSize, badgeSize, 0x80555555);
            }

            // 3. Render Workstation Item Icon
            graphics.renderItem(type.iconSupplier.get(), bx + 3, by + 3);

            // 4. Status Indicator Badge at bottom-right corner
            if (isPresent) {
                graphics.fill(bx + badgeSize - 6, by + badgeSize - 6, bx + badgeSize - 2, by + badgeSize - 2, 0xFF00E676);
            } else if (isMissingAndReq) {
                // Red Exclamation Badge
                graphics.fill(bx + badgeSize - 7, by + badgeSize - 7, bx + badgeSize - 1, by + badgeSize - 1, 0xFFFF1744);
                graphics.drawString(this.font, "!", bx + badgeSize - 5, by + badgeSize - 8, 0xFFFFFFFF, false);
            }

            // Check mouse hover
            if (mouseX >= bx && mouseX < bx + badgeSize && mouseY >= by && mouseY < by + badgeSize) {
                hoveredType = type;
                hoveredPresent = isPresent;
                hoveredMissingAndReq = isMissingAndReq;
            }
        }

        // Render Tooltip on hover
        if (hoveredType != null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(hoveredType.getDisplayName().copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

            if (hoveredMissingAndReq) {
                tooltip.add(Component.translatable("workstation.enchant_by_doing.status.required_missing")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            } else if (hoveredPresent) {
                tooltip.add(Component.translatable("workstation.enchant_by_doing.status.present")
                        .withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.translatable("workstation.enchant_by_doing.status.absent")
                        .withStyle(ChatFormatting.GRAY));
            }

            tooltip.add(Component.translatable("workstation.enchant_by_doing." + hoveredType.id + ".desc")
                    .withStyle(ChatFormatting.DARK_AQUA));

            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }
}