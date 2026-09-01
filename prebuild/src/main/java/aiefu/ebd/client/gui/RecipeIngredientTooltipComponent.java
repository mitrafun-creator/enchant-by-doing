package aiefu.ebd.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class RecipeIngredientTooltipComponent implements ClientTooltipComponent {
    private final ItemStack stack;
    private final Component text;

    public RecipeIngredientTooltipComponent(ItemStack stack, Component text) {
        this.stack = stack;
        this.text = text;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public int getWidth(Font font) {
        return 18 + font.width(this.text);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        guiGraphics.renderFakeItem(this.stack, x, y + 1);
        guiGraphics.drawString(font, this.text, x + 18, y + 5, -1, false);
    }
}
