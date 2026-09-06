package aiefu.ebd.mixin;

import aiefu.ebd.LBDConfig;
import aiefu.ebd.network.S2CWorkstationStatusPayload;
import aiefu.ebd.workstation.WorkstationHelper;
import aiefu.ebd.workstation.WorkstationType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

    @Shadow @Final private CraftingContainer craftSlots;
    @Shadow @Final private ResultContainer resultSlots;
    @Shadow @Final public ContainerLevelAccess access;
    @Shadow @Final private Player player;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void onInit(int containerId, Inventory playerInventory, ContainerLevelAccess access, CallbackInfo ci) {
        if (!this.player.level().isClientSide() && this.player instanceof ServerPlayer sp && sp.connection != null && !(sp instanceof net.neoforged.neoforge.common.util.FakePlayer)) {
            if (!LBDConfig.INSTANCE.enableCraftingWorkstations) return;
            this.access.execute((level, pos) -> {
                int radius = LBDConfig.INSTANCE.workstationDetectionRadius;
                byte nearbyMask = WorkstationHelper.getNearbyWorkstationsMask(level, pos, radius);
                PacketDistributor.sendToPlayer(sp, new S2CWorkstationStatusPayload(nearbyMask, (byte) 0));
            });
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(Player player, CallbackInfo ci) {
        if (!player.level().isClientSide() && player instanceof ServerPlayer sp && sp.connection != null && !(sp instanceof net.neoforged.neoforge.common.util.FakePlayer)) {
            PacketDistributor.sendToPlayer(sp, new S2CWorkstationStatusPayload((byte) 0, (byte) 0));
        }
    }

    @Inject(method = "slotsChanged", at = @At("TAIL"))
    private void onSlotsChanged(Container container, CallbackInfo ci) {
        if (!this.player.level().isClientSide() && this.player instanceof ServerPlayer sp && sp.connection != null && !(sp instanceof net.neoforged.neoforge.common.util.FakePlayer)) {
            if (!LBDConfig.INSTANCE.enableCraftingWorkstations) return;

            this.access.execute((level, pos) -> {
                int radius = LBDConfig.INSTANCE.workstationDetectionRadius;
                byte nearbyMask = WorkstationHelper.getNearbyWorkstationsMask(level, pos, radius);

                ItemStack currentResult = this.resultSlots.getItem(0);
                byte reqMask = 0;

                if (!currentResult.isEmpty()) {
                    reqMask = WorkstationHelper.getRequiredWorkstationsMask(currentResult);
                    byte missingMask = (byte) (reqMask & ~nearbyMask);
                    if (missingMask != 0) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                    }
                } else {
                    // Check if craftSlots match a recipe whose result was blocked
                    CraftingInput input = this.craftSlots.asCraftInput();
                    if (!input.isEmpty()) {
                        Optional<RecipeHolder<CraftingRecipe>> match = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
                        if (match.isPresent()) {
                            ItemStack potentialResult = match.get().value().assemble(input, level.registryAccess());
                            reqMask = WorkstationHelper.getRequiredWorkstationsMask(potentialResult);
                        }
                    }
                }

                PacketDistributor.sendToPlayer(sp, new S2CWorkstationStatusPayload(nearbyMask, reqMask));
            });
        }
    }
}