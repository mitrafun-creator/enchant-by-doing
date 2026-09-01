package aiefu.ebd.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityTickerAccessor {
    @Accessor("attackStrengthTicker")
    void ebd$setAttackStrengthTicker(int value);
}
