package aiefu.ebd.mixin;

import aiefu.ebd.Utils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Arrow.class)
public class ArrowMixin {
    @Redirect(
        method = "doPostHurtEffects",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"
        )
    )
    private boolean onAddEffect(LivingEntity target, MobEffectInstance effect, Entity source) {
        Arrow arrow = (Arrow) (Object) this;
        Entity owner = arrow.getOwner();
        if (owner instanceof Player player) {
            int hunterLevel = Utils.getSkillLevel(player, "hunter");
            if (hunterLevel >= 75) {
                int newDuration = effect.getDuration() * 2;
                ((aiefu.ebd.mixin.MobEffectInstanceAccessor) effect).ebd$setDuration(newDuration);
            }
        }
        return target.addEffect(effect, source);
    }
}
