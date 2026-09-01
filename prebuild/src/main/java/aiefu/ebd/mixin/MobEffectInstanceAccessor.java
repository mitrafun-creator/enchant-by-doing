package aiefu.ebd.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobEffectInstance.class)
public interface MobEffectInstanceAccessor {
    @Accessor("duration")
    void ebd$setDuration(int duration);

    @Accessor("amplifier")
    void ebd$setAmplifier(int amplifier);
}
