package aiefu.ebd.mixin;

import aiefu.ebd.IBoatEnchanted;
import aiefu.ebd.Utils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Миксин на Entity — здесь объявлены hurt() и removePassenger().
 * Обрабатывает:
 *  - Негорит: иммунитет к огню (через hurt)
 *  - Вёсла в руки: автосбор лодки при выходе (через removePassenger)
 */
@Mixin(Entity.class)
public abstract class EntityMixin {

    // ============================
    // НЕГОРИТ — иммунитет к огню
    // ============================

    @Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
    private void ebd$fireImmune(CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof Boat boat) {
            if (boat instanceof IBoatEnchanted enchanted && Utils.getEnchantmentLevel(enchanted.ebd$getBoatEnchantments(), "fireproof_boat") > 0) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void ebd$onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object)this instanceof Boat boat)) return;
        if (!(boat instanceof IBoatEnchanted enchanted)) return;
        if (Utils.getEnchantmentLevel(enchanted.ebd$getBoatEnchantments(), "fireproof_boat") <= 0) return;

        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "displayFireAnimation", at = @At("HEAD"), cancellable = true)
    private void ebd$displayFireAnimation(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;
        if (self instanceof Boat boat) {
            if (boat instanceof IBoatEnchanted enchanted && Utils.getEnchantmentLevel(enchanted.ebd$getBoatEnchantments(), "fireproof_boat") > 0) {
                cir.setReturnValue(false);
            }
        } else if (self.getVehicle() instanceof Boat boat) {
            if (boat instanceof IBoatEnchanted enchanted && Utils.getEnchantmentLevel(enchanted.ebd$getBoatEnchantments(), "fireproof_boat") > 0) {
                cir.setReturnValue(false);
            }
        }
    }

    // ============================
    // ВЁСЛА В РУКИ — автосбор при выходе
    // ============================

    @Inject(method = "removePassenger", at = @At("HEAD"))
    private void ebd$onRemovePassenger(Entity passenger, CallbackInfo ci) {
        if (!((Object)this instanceof Boat boat)) return;
        if (boat.isRemoved()) return;
        if (!(boat instanceof IBoatEnchanted enchanted)) return;
        if (passenger instanceof net.minecraft.server.level.ServerPlayer sp && sp.hasDisconnected()) return;

        ItemEnchantments enc = enchanted.ebd$getBoatEnchantments();

        // Снимаем эффекты Субмарины при выходе
        if (Utils.getEnchantmentLevel(enc, "submarine") > 0 && passenger instanceof net.minecraft.world.entity.LivingEntity living && !boat.level().isClientSide()) {
            living.removeEffect(net.minecraft.world.effect.MobEffects.WATER_BREATHING);
            living.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
        }

        if (Utils.getEnchantmentLevel(enc, "pocket_boat") <= 0) return;
        if (boat.level().isClientSide()) return;
        if (!(passenger instanceof Player player)) return;

        // Создаём предмет лодки с зачарованиями
        ItemStack boatItem = new ItemStack(boat.getDropItem());
        if (!enc.isEmpty()) {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            for (Holder<Enchantment> h : enc.keySet()) {
                mutable.set(h, enc.getLevel(h));
            }
            boatItem.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        }

        // Выдаём игроку или бросаем рядом
        if (!player.getInventory().add(boatItem)) {
            player.drop(boatItem, false);
        }

        // Убираем лодку из мира (она уже стала предметом)
        boat.discard();
    }

    @Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"))
    private void ebd$injectEnchantmentsToSpawn(ItemStack stack, float offset, CallbackInfoReturnable<?> cir) {
        if ((Object)this instanceof Boat boat) {
            if (boat instanceof IBoatEnchanted enchantedBoat) {
                ItemEnchantments enchantments = enchantedBoat.ebd$getBoatEnchantments();
                if (enchantments != null && !enchantments.isEmpty()) {
                    stack.set(DataComponents.ENCHANTMENTS, enchantments);
                }
            }
        }
    }

}
