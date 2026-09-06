package aiefu.ebd.mixin;

import aiefu.ebd.network.ClientsideNetworkManager;
import aiefu.ebd.workstation.WorkstationHelper;
import aiefu.ebd.workstation.WorkstationType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecipeButton.class)
public abstract class RecipeButtonMixin {

    @Shadow public abstract RecipeHolder<?> getRecipe();
    @Shadow public abstract RecipeCollection getCollection();

    @Inject(method = "getTooltipText", at = @At("RETURN"))
    private void onGetTooltipText(CallbackInfoReturnable<List<Component>> cir) {
        RecipeHolder<?> recipeHolder = this.getRecipe();
        RecipeCollection collection = this.getCollection();
        if (recipeHolder == null || collection == null) return;

        ItemStack result = recipeHolder.value().getResultItem(collection.registryAccess());
        if (result == null || result.isEmpty()) return;

        byte reqMask = WorkstationHelper.getRequiredWorkstationsMask(result);
        if (reqMask == 0) return;

        List<Component> tooltip = cir.getReturnValue();
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("workstation.enchant_by_doing.recipe_book_header").withStyle(ChatFormatting.GRAY));

        byte nearbyMask = ClientsideNetworkManager.clientNearbyWorkstationsMask;
        if (nearbyMask == 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                nearbyMask = WorkstationHelper.getNearbyWorkstationsMask(
                        mc.level,
                        mc.player.blockPosition(),
                        aiefu.ebd.LBDConfig.INSTANCE.workstationDetectionRadius
                );
            }
        }

        for (WorkstationType type : WorkstationType.values()) {
            if (WorkstationHelper.isWorkstationRequired(reqMask, type)) {
                boolean isPresent = WorkstationHelper.isWorkstationPresent(nearbyMask, type);
                if (isPresent) {
                    tooltip.add(Component.literal(" ✔ ").withStyle(ChatFormatting.DARK_GREEN)
                            .append(type.getDisplayName().copy().withStyle(ChatFormatting.GREEN)));
                } else {
                    tooltip.add(Component.literal(" ✖ ").withStyle(ChatFormatting.DARK_RED)
                            .append(type.getDisplayName().copy().withStyle(ChatFormatting.RED)));
                }
            }
        }
    }
}
