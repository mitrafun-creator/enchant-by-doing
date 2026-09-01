package aiefu.ebd.mixin;

import aiefu.ebd.Utils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
    @Shadow
    protected abstract Projectile createProjectile(Level level, LivingEntity shooter, ItemStack crossbowStack, ItemStack projectileStack, boolean critical);

    @Shadow
    protected abstract void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float divergence, float soundPitch, LivingEntity target);

    @Inject(method = "releaseUsing", at = @At("TAIL"))
    private void onReleaseUsing(ItemStack stack, Level level, LivingEntity shooter, int timeCharged, CallbackInfo ci) {
        if (level.isClientSide()) return;
        
        int magazineLevel = Utils.getEnchantmentLevel(stack, "magazine");
        if (magazineLevel <= 0) return;

        ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (charged == null || charged.isEmpty()) return;

        List<ItemStack> loaded = new ArrayList<>(charged.getItems());
        
        var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var multishotHolder = registry.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.MULTISHOT);
        int multishotLevel = stack.getEnchantments().getLevel(multishotHolder);
        int shotCount = multishotLevel > 0 ? 3 : 1;
        
        int targetSize = (magazineLevel + 1) * shotCount;
        
        boolean loadedAny = false;
        while (loaded.size() < targetSize) {
            ItemStack ammo = shooter.getProjectile(stack);
            if (ammo.isEmpty()) {
                break;
            }
            
            ItemStack loadedAmmo = ammo.copy();
            loadedAmmo.setCount(1);
            loaded.add(loadedAmmo);
            
            if (!(shooter instanceof net.minecraft.world.entity.player.Player player && player.getAbilities().instabuild)) {
                ammo.shrink(1);
                if (ammo.isEmpty() && shooter instanceof net.minecraft.world.entity.player.Player player2) {
                    player2.getInventory().removeItem(ammo);
                }
            }
            loadedAny = true;
        }
        
        if (loadedAny) {
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(loaded));
            if (shooter instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                serverPlayer.containerMenu.broadcastChanges();
            }
        }
    }

    @Inject(method = "performShooting", at = @At("HEAD"), cancellable = true)
    private void onPerformShooting(Level level, LivingEntity shooter, InteractionHand hand, ItemStack stack, float velocity, float divergence, LivingEntity target, CallbackInfo ci) {
        // Только на сервере — клиент пусть отрабатывает ванильно (анимации, звуки)
        if (level.isClientSide()) return;

        int magazineLevel = Utils.getEnchantmentLevel(stack, "magazine");
        if (magazineLevel <= 0) return;

        // Отменяем ванильный метод только на сервере
        ci.cancel();

        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        ChargedProjectiles chargedProjectiles = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles == null || chargedProjectiles.isEmpty()) return;

        List<ItemStack> list = chargedProjectiles.getItems();
        if (list.isEmpty()) return;

        var registry = serverLevel.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var multishotHolder = registry.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.MULTISHOT);
        int multishotLevel = stack.getEnchantments().getLevel(multishotHolder);
        int shotCount = multishotLevel > 0 ? 3 : 1;

        int toShoot = Math.min(shotCount, list.size());

        // Spread для multishot — 10 градусов стандарт, или через enchantment если есть
        float spread = 10.0F;
        float f1 = toShoot == 1 ? 0.0F : 2.0F * spread / (float)(toShoot - 1);
        float f2 = (float)((toShoot - 1) % 2) * f1 / 2.0F;
        float f3 = 1.0F;

        for (int i = 0; i < toShoot; i++) {
            ItemStack projectileStack = list.get(i);
            if (!projectileStack.isEmpty()) {
                float angleOffset = f2 + f3 * (float)((i + 1) / 2) * f1;
                f3 = -f3;

                Projectile projectile = createProjectile(serverLevel, shooter, stack, projectileStack, true);
                shootProjectile(shooter, projectile, i, velocity, divergence, angleOffset, target);
                serverLevel.addFreshEntity(projectile);
            }
        }

        List<ItemStack> remaining = new ArrayList<>();
        for (int i = toShoot; i < list.size(); i++) {
            remaining.add(list.get(i).copy());
        }

        if (remaining.isEmpty()) {
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        } else {
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(remaining));
        }

        if (shooter instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            stack.hurtAndBreak(1, serverLevel, serverPlayer, item -> {});
            serverPlayer.containerMenu.broadcastChanges();
        }
    }

    /**
     * Обойма замедляет перезарядку:
     *   Уровень 1 → ×1.25 (на 25% медленнее)
     *   Уровень 2 → ×1.50 (на 50% медленнее)
     * Quick Charge применяется ДО нашего множителя, так что оба зачарования
     * корректно взаимодействуют (Quick Charge снижает базу, обойма её умножает).
     */
    @Inject(method = "getChargeDuration", at = @At("RETURN"), cancellable = true)
    private static void onGetChargeDuration(ItemStack crossbow, LivingEntity entity,
                                            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Integer> cir) {
        int base = cir.getReturnValue();
        float multiplier = 1.0F;

        int magazineLevel = Utils.getEnchantmentLevel(crossbow, "magazine");
        if (magazineLevel > 0) {
            multiplier += 0.25F * magazineLevel;
        }

        if (entity instanceof net.minecraft.world.entity.player.Player player) {
            int hunterLevel = Utils.getSkillLevel(player, "hunter");
            if (hunterLevel >= 50) {
                float hunterMultiplier = 1.25F;
                if (hunterLevel >= 100) {
                    hunterMultiplier = 1.50F;
                }
                multiplier /= hunterMultiplier;
            }
        }

        cir.setReturnValue(Math.round(base * multiplier));
    }
}
