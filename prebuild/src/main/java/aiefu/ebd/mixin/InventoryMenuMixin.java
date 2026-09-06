package aiefu.ebd.mixin;

import aiefu.ebd.LBDConfig;
import aiefu.ebd.network.S2CWorkstationStatusPayload;
import aiefu.ebd.workstation.WorkstationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {

    @Shadow @Final private CraftingContainer craftSlots;
    @Shadow @Final private ResultContainer resultSlots;
    @Shadow @Final private Player owner;

    @Inject(method = "slotsChanged", at = @At("TAIL"))
    private void onSlotsChanged(Container container, CallbackInfo ci) {
        if (!this.owner.level().isClientSide() && this.owner instanceof ServerPlayer sp && sp.connection != null && !(sp instanceof net.neoforged.neoforge.common.util.FakePlayer)) {
            if (!LBDConfig.INSTANCE.enableCraftingWorkstations) return;

            Level level = this.owner.level();
            BlockPos pos = this.owner.blockPosition();
            int radius = LBDConfig.INSTANCE.workstationDetectionRadius;
            byte nearbyMask = WorkstationHelper.getNearbyWorkstationsMask(level, pos, radius);

            ItemStack currentResult = this.resultSlots.getItem(0);
            byte reqMask = 0;

            if (!currentResult.isEmpty()) {
                byte potentialReq = WorkstationHelper.getRequiredWorkstationsMask(currentResult);
                byte missingMask = (byte) (potentialReq & ~nearbyMask);
                if (missingMask != 0) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                }
                reqMask = potentialReq;
            } else {
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
        }
    }
}