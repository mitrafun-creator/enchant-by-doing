package aiefu.ebd.client.gui;

import aiefu.ebd.data.RecipeHolder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import java.util.List;

public class EnchButtonWithData extends CustomEnchantingButton {

    @Nullable
    protected RecipeHolder recipe;
    protected EnchantmentInstance enchantmentInstance;
    protected int ordinal;
    
    protected boolean incompatible = false;
    protected int targetLevel = 1;
    protected List<ClientTooltipComponent> cachedTooltip;

    public EnchButtonWithData(int x, int y, int width, int height, Component message, OnPress onPress, RecipeHolder recipe, Holder<Enchantment> enchantment, int level, int ordinal) {
        super(x, y, width, height, message, onPress);
        this.recipe = recipe;
        this.enchantmentInstance = new EnchantmentInstance(enchantment, level);
        this.ordinal = ordinal;
    }

    public @Nullable RecipeHolder getRecipe() {
        return recipe;
    }

    public Holder<Enchantment> getEnchantment() {
        return enchantmentInstance.enchantment;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public boolean isIncompatible() {
        return incompatible;
    }

    public void setIncompatible(boolean incompatible) {
        this.incompatible = incompatible;
    }

    public int getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    public List<ClientTooltipComponent> getCachedTooltip() {
        return cachedTooltip;
    }

    public void setCachedTooltip(List<ClientTooltipComponent> cachedTooltip) {
        this.cachedTooltip = cachedTooltip;
    }

    @Override
    public void drawCenteredString(GuiGraphics graphics, Font font, Component text, int color) {
        if (this.incompatible) {
            text = text.copy().withStyle(net.minecraft.ChatFormatting.STRIKETHROUGH).withStyle(net.minecraft.ChatFormatting.DARK_GRAY);
            color = 0x555555;
        }
        super.drawCenteredString(graphics, font, text, color);
    }
}
