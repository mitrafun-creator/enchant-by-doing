package aiefu.ebd.mixin;

import aiefu.ebd.IBoatEnchanted;
import aiefu.ebd.Utils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Перехватывает использование предмета-лодки (right click) и переносит
 * зачарования с предмета на только что спавненную сущность лодки.
 */
@Mixin(BoatItem.class)
public abstract class BoatItemMixin {

    @Inject(method = "getBoat", at = @At("RETURN"))
    private void ebd$transferEnchantments(Level level, net.minecraft.world.phys.HitResult hitResult, ItemStack stack, Player player,
                                          CallbackInfoReturnable<Boat> cir) {
        Boat boat = cir.getReturnValue();
        if (boat instanceof IBoatEnchanted enchantedBoat) {
            enchantedBoat.ebd$setBoatEnchantments(stack.getEnchantments());
        }
    }

    @org.spongepowered.asm.mixin.injection.Redirect(
        method = "use",
        at = @org.spongepowered.asm.mixin.injection.At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"
        )
    )
    private boolean ebd$allowSubmarineCollision(Level level, Entity entity, AABB box) {
        if (entity instanceof IBoatEnchanted enchantedBoat) {
            if (Utils.getEnchantmentLevel(enchantedBoat.ebd$getBoatEnchantments(), "submarine") > 0) {
                return level.noBlockCollision(entity, box);
            }
        }
        return level.noCollision(entity, box);
    }
}
