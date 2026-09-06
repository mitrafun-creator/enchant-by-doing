package aiefu.ebd.mixin;

import aiefu.ebd.LBDConfig;
import aiefu.ebd.network.S2CWorkstationStatusPayload;
import aiefu.ebd.workstation.WorkstationHelper;
import aiefu.ebd.workstation.WorkstationType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void onQuickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index == 0) {
            ItemStack result = this.resultSlots.getItem(0);
            if (!result.isEmpty()) {
                if (aiefu.ebd.GlobalPerks.isItemLockedForPlayer(player, result)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
                if (LBDConfig.INSTANCE.enableCraftingWorkstations) {
                    byte reqMask = WorkstationHelper.getRequiredWorkstationsMask(result);
                    if (reqMask != 0) {
                        int radius = LBDConfig.INSTANCE.workstationDetectionRadius;
                        byte nearbyMask = WorkstationHelper.getNearbyWorkstationsMask(player.level(), player.blockPosition(), radius);
                        if ((reqMask & ~nearbyMask) != 0) {
                            cir.setReturnValue(ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "slotChangedCraftingGrid", at = @At("TAIL"))
    private static void onSlotChangedCraftingGrid(
            AbstractContainerMenu menu,
            net.minecraft.world.level.Level level,
            Player player,
            CraftingContainer craftSlots,
            ResultContainer resultSlots,
            RecipeHolder<CraftingRecipe> recipe,
            CallbackInfo ci
    ) {
        if (level.isClientSide()) return;
        if (!(player instanceof ServerPlayer sp) || sp.connection == null || (sp instanceof net.neoforged.neoforge.common.util.FakePlayer)) return;

        ItemStack currentResult = resultSlots.getItem(0);
        if (!currentResult.isEmpty() && aiefu.ebd.GlobalPerks.isItemLockedForPlayer(sp, currentResult)) {
            resultSlots.setItem(0, ItemStack.EMPTY);
            menu.setRemoteSlot(0, ItemStack.EMPTY);
            sp.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(
                    menu.containerId, menu.incrementStateId(), 0, ItemStack.EMPTY
            ));
            aiefu.ebd.GlobalPerks.Perk perk = aiefu.ebd.GlobalPerks.getRequiredPerk(currentResult);
            if (perk != null) {
                sp.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c🔒 Для создания этого предмета требуется перк: §6")
                                .append(perk.getDisplayName())
                                .append(" §c(§e" + perk.costPerLevel + " очк.§c)"),
                        true
                );
            }
            return;
        }

        if (!LBDConfig.INSTANCE.enableCraftingWorkstations) return;

        BlockPos pos = sp.blockPosition();
        if (menu instanceof CraftingMenu) {
            pos = ((CraftingMenuMixin) (Object) menu).access.evaluate((lvl, p) -> p).orElse(sp.blockPosition());
        }
        int radius = LBDConfig.INSTANCE.workstationDetectionRadius;
        byte nearbyMask = WorkstationHelper.getNearbyWorkstationsMask(level, pos, radius);

        currentResult = resultSlots.getItem(0);
        byte reqMask = 0;

        if (!currentResult.isEmpty()) {
            reqMask = WorkstationHelper.getRequiredWorkstationsMask(currentResult);
            byte missingMask = (byte) (reqMask & ~nearbyMask);
            if (missingMask != 0) {
                resultSlots.setItem(0, ItemStack.EMPTY);
                menu.setRemoteSlot(0, ItemStack.EMPTY);
                sp.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(
                        menu.containerId, menu.incrementStateId(), 0, ItemStack.EMPTY
                ));
            }
        } else {
            CraftingInput input = craftSlots.asCraftInput();
            if (!input.isEmpty()) {
                Optional<RecipeHolder<CraftingRecipe>> match = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
                if (match.isPresent()) {
                    ItemStack potentialResult = match.get().value().assemble(input, level.registryAccess());
                    reqMask = WorkstationHelper.getRequiredWorkstationsMask(potentialResult);
                }
            }
        }

        PacketDistributor.sendToPlayer(sp, new S2CWorkstationStatusPayload(nearbyMask, reqMask));
    }
}