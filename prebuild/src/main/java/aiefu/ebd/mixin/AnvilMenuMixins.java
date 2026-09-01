package aiefu.ebd.mixin;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.Utils;
import aiefu.ebd.data.materialoverrides.MaterialData;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixins extends ItemCombinerMenu {

    @Shadow @Final private DataSlot cost;

    public AnvilMenuMixins(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    @Inject(method = "createResult",at = @At(value = "INVOKE", target = "net/minecraft/world/inventory/AnvilMenu.broadcastChanges()V", shift = At.Shift.BEFORE))
    public void patchResultStack(CallbackInfo ci){
        ItemStack stack = this.resultSlots.getItem(0);
        if(!stack.isEmpty()) {
            ItemStack leftInput = this.inputSlots.getItem(0);
            ItemStack rightInput = this.inputSlots.getItem(1);
            if (!leftInput.isEmpty() && !rightInput.isEmpty()) {
                resolveEnchantmentConflicts(stack, leftInput, rightInput);
            }
        }

        if(!this.player.getAbilities().instabuild){
            if(!stack.isEmpty() && Utils.containsEnchantments(stack)){
                ItemStack input = this.inputSlots.getItem(0);

                net.minecraft.world.item.enchantment.ItemEnchantments enchsComponent = EnchantmentHelper.getEnchantmentsForCrafting(stack);
                Map<Holder<Enchantment>, Integer> enchs = new HashMap<>();
                enchsComponent.entrySet().forEach(entry -> enchs.put(entry.getKey(), entry.getValue()));

                net.minecraft.world.item.enchantment.ItemEnchantments inputComponent = EnchantmentHelper.getEnchantmentsForCrafting(input);
                Map<Holder<Enchantment>, Integer> inputE = new HashMap<>();
                inputComponent.entrySet().forEach(entry -> inputE.put(entry.getKey(), entry.getValue()));

                if(!Utils.containsSameEnchantmentsOfSameLevel(enchs, inputE)){
                    if(EBDCommon.config.disableAnvilEnchanting){
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                    } else if (EBDCommon.config.disableBookCombining && stack.is(Items.ENCHANTED_BOOK)) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                    } else {
                        MaterialData data = Utils.getMatData(stack.getItem());
                        Map<Holder<Enchantment>, Integer> curses = Utils.filterToNewMap(enchs, (e, i) -> e.is(net.minecraft.tags.EnchantmentTags.CURSE));
                        int limit = Utils.getEnchantmentsLimit(this.player, curses.size(), data);

                        if(stack.isDamageableItem() && input.isDamageableItem() && stack.getDamageValue() != input.getDamageValue()){
                            net.minecraft.world.item.enchantment.ItemEnchantments.Mutable builder = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                            inputE.forEach(builder::set);
                            EnchantmentHelper.setEnchantments(stack, builder.toImmutable());
                        } else if(enchs.size() > limit + curses.size()){
                            this.resultSlots.setItem(0, ItemStack.EMPTY);
                            this.cost.set(0);
                        }
                    }
                }
            }
        }
    }

    @org.spongepowered.asm.mixin.Unique
    private void resolveEnchantmentConflicts(ItemStack resultStack, ItemStack leftInput, ItemStack rightInput) {
        net.minecraft.world.item.enchantment.ItemEnchantments resultEnchs = EnchantmentHelper.getEnchantmentsForCrafting(resultStack);
        net.minecraft.world.item.enchantment.ItemEnchantments.Mutable cleaned = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(resultEnchs);
        
        java.util.List<Holder<Enchantment>> list = new java.util.ArrayList<>(resultEnchs.keySet());
        boolean modified = false;
        
        for (int i = 0; i < list.size(); i++) {
            Holder<Enchantment> e1 = list.get(i);
            if (cleaned.getLevel(e1) == 0) continue;
            
            for (int j = i + 1; j < list.size(); j++) {
                Holder<Enchantment> e2 = list.get(j);
                if (cleaned.getLevel(e2) == 0) continue;
                
                if (!Enchantment.areCompatible(e1, e2)) {
                    boolean e1FromRight = rightInput.getEnchantments().getLevel(e1) > 0;
                    boolean e2FromRight = rightInput.getEnchantments().getLevel(e2) > 0;
                    
                    if (e1FromRight && !e2FromRight) {
                        cleaned.set(e2, 0);
                        modified = true;
                    } else if (e2FromRight && !e1FromRight) {
                        cleaned.set(e1, 0);
                        modified = true;
                    } else {
                        int l1 = resultEnchs.getLevel(e1);
                        int l2 = resultEnchs.getLevel(e2);
                        if (l1 >= l2) {
                            cleaned.set(e2, 0);
                        } else {
                            cleaned.set(e1, 0);
                        }
                        modified = true;
                    }
                }
            }
        }
        if (modified) {
            EnchantmentHelper.setEnchantments(resultStack, cleaned.toImmutable());
        }
    }

    @org.spongepowered.asm.mixin.injection.Redirect(
        method = "createResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/Enchantment;areCompatible(Lnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)Z"
        )
    )
    private boolean ebd$areCompatible(Holder<Enchantment> holder1, Holder<Enchantment> holder2) {
        return true;
    }

    @org.spongepowered.asm.mixin.Unique
    private int ebd$originalLevel = -1;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void ebd$init(int containerId, Inventory playerInventory, ContainerLevelAccess access, CallbackInfo ci) {
        Player player = playerInventory.player;
        this.ebd$originalLevel = player.experienceLevel;
        player.experienceLevel = 100;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.ebd$originalLevel != -1) {
            player.experienceLevel = this.ebd$originalLevel;
            this.ebd$originalLevel = -1;
        }
    }

    @org.spongepowered.asm.mixin.injection.Redirect(
        method = "createResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V"
        )
    )
    private void ebd$overrideAnvilCost(DataSlot dataSlot, int value) {
        if (dataSlot == this.cost) {
            dataSlot.set(0);
        } else {
            dataSlot.set(value);
        }
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "net/minecraft/world/inventory/AnvilMenu.broadcastChanges()V"))
    private void forceZeroCost(CallbackInfo ci) {
        this.cost.set(0);
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    protected void patchMayPickup(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        if (this.cost.get() == 0 && !this.resultSlots.getItem(0).isEmpty()) {
            cir.setReturnValue(true);
        }
    }
}
