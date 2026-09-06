package aiefu.ebd.client.gui;

import aiefu.ebd.network.ClientsideNetworkManager;
import aiefu.ebd.workstation.WorkstationHelper;
import aiefu.ebd.workstation.WorkstationType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class WorkstationGuiRenderer {

    public static void renderBadges(GuiGraphics graphics, Font font, int startX, int startY, int mouseX, int mouseY) {
        byte nearbyMask = ClientsideNetworkManager.clientNearbyWorkstationsMask;
        byte missingMask = ClientsideNetworkManager.clientMissingWorkstationsMask;

        int slotSize = 20;
        WorkstationType missingHoveredType = null;

        WorkstationType[] types = WorkstationType.values();
        for (int i = 0; i < types.length; i++) {
            WorkstationType type = types[i];
            int bx = startX;
            int by = startY + i * (slotSize + 3);

            boolean isPresent = WorkstationHelper.isWorkstationPresent(nearbyMask, type);
            boolean isMissingAndReq = WorkstationHelper.isWorkstationRequiredAndMissing(missingMask, type);

            // 1. Vanilla-style slot background
            graphics.fill(bx, by, bx + slotSize, by + slotSize, 0x90000000);
            graphics.renderOutline(bx, by, slotSize, slotSize, 0x40555555);

            // 2. Alert or Status indicator
            if (isMissingAndReq) {
                // Pulsing Red Outline
                float pulse = (float) Math.sin(System.currentTimeMillis() * 0.008f);
                int alpha = (int) (180 + 75 * pulse);
                int redColor = (alpha << 24) | 0xFF2222;
                graphics.renderOutline(bx, by, slotSize, slotSize, redColor);
                graphics.renderOutline(bx - 1, by - 1, slotSize + 2, slotSize + 2, ((alpha / 2) << 24) | 0xFF4444);

                // Red Exclamation Badge
                graphics.fill(bx + slotSize - 6, by + slotSize - 6, bx + slotSize - 1, by + slotSize - 1, 0xFFFF1744);
                graphics.drawString(font, "!", bx + slotSize - 5, by + slotSize - 7, 0xFFFFFFFF, false);
            } else if (isPresent) {
                // Subtle green corner indicator for active stations
                graphics.fill(bx + slotSize - 5, by + slotSize - 5, bx + slotSize - 2, by + slotSize - 2, 0xFF00E676);
            }

            // 3. Render Workstation Item Icon
            graphics.renderItem(type.iconSupplier.get(), bx + 2, by + 2);

            // Check mouse hover ONLY if this station is missing and required
            if (isMissingAndReq && mouseX >= bx && mouseX < bx + slotSize && mouseY >= by && mouseY < by + slotSize) {
                missingHoveredType = type;
            }
        }

        // Render Tooltip ONLY if a missing required station is hovered
        if (missingHoveredType != null) {
            Component tooltip = Component.translatable("workstation.enchant_by_doing.required_tooltip", missingHoveredType.getDisplayName())
                    .withStyle(ChatFormatting.RED);
            graphics.renderTooltip(font, tooltip, mouseX, mouseY);
        }
    }
}