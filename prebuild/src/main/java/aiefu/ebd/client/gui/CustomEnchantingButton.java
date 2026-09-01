package aiefu.ebd.client.gui;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.client.EBDClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.Objects;

public class CustomEnchantingButton extends Button {
    public static final ResourceLocation ench_buttons = ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "textures/gui/ench_buttons.png");
    public CustomEnchantingButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        guiGraphics.blitWithBorder(ench_buttons, this.getX(), this.getY(), 0, this.getTextureY(), this.getWidth(), this.getHeight(), 200, 20, 4, 4, 20, 20);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.active ? EBDClient.colorData.getTextActiveColor() : EBDClient.colorData.getTextInactiveColor();
        this.drawCenteredString(guiGraphics, minecraft.font, this.getMessage(), i | Mth.ceil(this.alpha * 255.0F) << 24);
        //this.renderString(guiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void drawCenteredString(GuiGraphics graphics, Font font, Component text, int color){
        Objects.requireNonNull(font);

        int l = font.width(text);
        int minX = this.getX() + this.width;
        int r = (this.getY() + this.getY() + this.getHeight() - 9) / 2 + 1;
        int centerX = (minX + this.getX()) / 2;

        float scale = 1.0F;
        int maxWidth = this.width - 6;
        if (l > maxWidth) {
            scale = (float) maxWidth / (float) l;
            if (scale < 0.5F) {
                scale = 0.5F;
            }
        }

        if (scale < 1.0F) {
            graphics.pose().pushPose();
            graphics.pose().translate((float) centerX, (float) r + 4.5F, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.pose().translate(-((float) centerX), -((float) r + 4.5F), 0.0F);
            this.drawCenteredString(graphics, font, text, centerX, r, color, false);
            graphics.pose().popPose();
        } else {
            this.drawCenteredString(graphics, font, text, centerX, r, color, false);
        }
    }

    public void drawCenteredString(GuiGraphics graphics, Font font, Component text, int x, int y, int color, boolean shadow){
        FormattedCharSequence formattedCharSequence = text.getVisualOrderText();
        graphics.drawString(font, formattedCharSequence, x - font.width(formattedCharSequence) / 2, y, color, shadow);
    }

    public int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isHoveredOrFocused()) { //W148 H197
            i = 2;
        }

        return i * 20;
    }

    public boolean myHovered = false;

    @Override
    public boolean isHovered() {
        return this.myHovered;
    }

}
