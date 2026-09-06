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
        byte requiredMask = ClientsideNetworkManager.clientRequiredWorkstationsMask;

        int slotSize = 18;
        WorkstationType missingHoveredType = null;
        int visibleIndex = 0;

        for (WorkstationType type : WorkstationType.values()) {
            boolean isPresent = WorkstationHelper.isWorkstationPresent(nearbyMask, type);
            boolean isRequired = WorkstationHelper.isWorkstationRequired(requiredMask, type);

            // 1. Only display if present nearby OR required for current craft
            if (!isPresent && !isRequired) {
                continue;
            }

            boolean isMissing = isRequired && !isPresent;
            int bx = startX;
            int by = startY + visibleIndex * (slotSize + 2);
            visibleIndex++;

            // 2. Subtle translucent slot background for readability (no border)
            graphics.fill(bx, by, bx + slotSize, by + slotSize, 0x50000000);

            // 3. Status indicator: Red outline ONLY when missing & required
            if (isMissing) {
                float pulse = (float) Math.sin(System.currentTimeMillis() * 0.008f);
                int alpha = (int) (190 + 65 * pulse);
                int redColor = (alpha << 24) | 0xFF2222;
                graphics.renderOutline(bx, by, slotSize, slotSize, redColor);
            }
            // Present workstations have NO outline and NO colored dots

            // 4. Render Workstation Item Icon (16x16 inside 18x18 slot)
            graphics.renderItem(type.iconSupplier.get(), bx + 1, by + 1);

            // 5. Check mouse hover ONLY if this station is missing and required
            if (isMissing && mouseX >= bx && mouseX < bx + slotSize && mouseY >= by && mouseY < by + slotSize) {
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