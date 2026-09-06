package aiefu.ebd.client.gui;

import aiefu.ebd.network.ClientsideNetworkManager;
import aiefu.ebd.workstation.WorkstationHelper;
import aiefu.ebd.workstation.WorkstationType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class WorkstationGuiRenderer {

    public static void renderBadges(GuiGraphics graphics, Font font, int startX, int startY, int mouseX, int mouseY) {
        byte nearbyMask = ClientsideNetworkManager.clientNearbyWorkstationsMask;
        byte missingMask = ClientsideNetworkManager.clientMissingWorkstationsMask;

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
            boolean isMissingAndReq = WorkstationHelper.isWorkstationRequiredAndMissing(missingMask, type);

            // 1. Draw Background Box
            graphics.fill(bx, by, bx + badgeSize, by + badgeSize, 0xD0181820);

            // 2. Draw Borders and Effects
            if (isMissingAndReq) {
                // Pulsing Red Outline
                float pulse = (float) Math.sin(System.currentTimeMillis() * 0.008f);
                int alpha = (int) (180 + 75 * pulse);
                int redColor = (alpha << 24) | 0xFF2222;
                graphics.renderOutline(bx, by, badgeSize, badgeSize, redColor);
                graphics.renderOutline(bx - 1, by - 1, badgeSize + 2, badgeSize + 2, ((alpha / 2) << 24) | 0xFF4444);
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
            if (isMissingAndReq) {
                // Red Exclamation Badge
                graphics.fill(bx + badgeSize - 7, by + badgeSize - 7, bx + badgeSize - 1, by + badgeSize - 1, 0xFFFF1744);
                graphics.drawString(font, "!", bx + badgeSize - 5, by + badgeSize - 8, 0xFFFFFFFF, false);
            } else if (isPresent) {
                // Green status dot
                graphics.fill(bx + badgeSize - 6, by + badgeSize - 6, bx + badgeSize - 2, by + badgeSize - 2, 0xFF00E676);
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

            graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }
}