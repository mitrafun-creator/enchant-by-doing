package aiefu.ebd.mixin;
import aiefu.ebd.Utils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void onGetUseDuration(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof AxeItem) {
            if (Utils.containsEnchantment(self, "leviathan")) {
                cir.setReturnValue(72000);
            }
        } else if (self.is(Items.FLINT_AND_STEEL)) {
            if (Utils.containsEnchantment(self, "dragons_breath")) {
                cir.setReturnValue(72000);
            }
        }
    }

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void onGetUseAnimation(CallbackInfoReturnable<UseAnim> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof AxeItem) {
            if (Utils.containsEnchantment(self, "leviathan")) {
                cir.setReturnValue(UseAnim.SPEAR);
            }
        } else if (self.is(Items.FLINT_AND_STEEL)) {
            if (Utils.containsEnchantment(self, "dragons_breath")) {
                cir.setReturnValue(UseAnim.NONE);
            }
        }
    }

    @Inject(method = "use", at = @At("HEAD"))
    private void ebd$beforeUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.is(Utils.ENCHANTABLE_BOAT) || self.getItem() instanceof net.minecraft.world.item.BoatItem || self.getItem().getClass().getSimpleName().contains("Boat")) {
            aiefu.ebd.EBDGameplayEvents.ACTIVE_PLACING_BOAT_ENCHANTMENTS.set(self.getEnchantments());
        }
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void ebd$afterUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.is(Utils.ENCHANTABLE_BOAT) || self.getItem() instanceof net.minecraft.world.item.BoatItem || self.getItem().getClass().getSimpleName().contains("Boat")) {
            aiefu.ebd.EBDGameplayEvents.ACTIVE_PLACING_BOAT_ENCHANTMENTS.set(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        }
    }

    @Inject(method = "useOn", at = @At("HEAD"))
    private void ebd$beforeUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.is(Utils.ENCHANTABLE_BOAT) || self.getItem() instanceof net.minecraft.world.item.BoatItem || self.getItem().getClass().getSimpleName().contains("Boat")) {
            aiefu.ebd.EBDGameplayEvents.ACTIVE_PLACING_BOAT_ENCHANTMENTS.set(self.getEnchantments());
        }
    }

    @Inject(method = "useOn", at = @At("RETURN"))
    private void ebd$afterUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.is(Utils.ENCHANTABLE_BOAT) || self.getItem() instanceof net.minecraft.world.item.BoatItem || self.getItem().getClass().getSimpleName().contains("Boat")) {
            aiefu.ebd.EBDGameplayEvents.ACTIVE_PLACING_BOAT_ENCHANTMENTS.set(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        }
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void onHurtAndBreakPlayer(int amount, net.minecraft.server.level.ServerLevel level, net.minecraft.server.level.ServerPlayer player, java.util.function.Consumer<net.minecraft.world.item.Item> onBroken, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof AxeItem && player != null) {
            if (Utils.getSkillLevel(player, "lumberjack") >= 80) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void onHurtAndBreakLiving(int amount, net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity entity, java.util.function.Consumer<net.minecraft.world.item.Item> onBroken, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof AxeItem && entity instanceof Player player) {
            if (Utils.getSkillLevel(player, "lumberjack") >= 80) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At("HEAD"), cancellable = true)
    private void onHurtAndBreakSlot(int amount, net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.entity.EquipmentSlot slot, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof AxeItem && entity instanceof Player player) {
            if (Utils.getSkillLevel(player, "lumberjack") >= 80) {
                ci.cancel();
            }
        }
    }
}

