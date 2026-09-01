package aiefu.ebd.mixin;

import aiefu.ebd.IBoatEnchanted;
import aiefu.ebd.Utils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VehicleEntity.class)
public abstract class VehicleEntityMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void ebd$onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof Boat boat) {
            if (boat instanceof IBoatEnchanted enchanted && Utils.getEnchantmentLevel(enchanted.ebd$getBoatEnchantments(), "fireproof_boat") > 0) {
                if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
