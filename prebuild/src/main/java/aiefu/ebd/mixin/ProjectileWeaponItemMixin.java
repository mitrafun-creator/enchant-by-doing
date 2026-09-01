package aiefu.ebd.mixin;

import aiefu.ebd.Utils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
    @ModifyVariable(
        method = "useAmmo(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Z)Lnet/minecraft/world/item/ItemStack;",
        at = @At("STORE"),
        ordinal = 0
    )
    private static int onUseAmmo(int original, ItemStack weaponStack, ItemStack ammoStack, LivingEntity shooter, boolean creative) {
        if (shooter instanceof Player player) {
            int hunterLevel = Utils.getSkillLevel(player, "hunter");
            if (hunterLevel >= 25) {
                double chance = 0.25;
                if (hunterLevel >= 75) {
                    chance = 0.50;
                }
                if (player.getRandom().nextDouble() < chance) {
                    return 0; // do not consume ammo
                }
            }
        }
        return original;
    }
}
