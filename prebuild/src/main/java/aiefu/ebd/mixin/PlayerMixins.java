package aiefu.ebd.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixins {
    @Shadow public int experienceLevel;
    @Shadow public int totalExperience;
    @Shadow public float experienceProgress;

    @Inject(method = "giveExperiencePoints", at = @At("HEAD"), cancellable = true)
    private void zeroExperiencePoints(int points, CallbackInfo ci) {
        this.experienceLevel = 0;
        this.totalExperience = 0;
        this.experienceProgress = 0.0f;
        ci.cancel();
    }

    @Inject(method = "giveExperienceLevels", at = @At("HEAD"), cancellable = true)
    private void zeroExperienceLevels(int levels, CallbackInfo ci) {
        this.experienceLevel = 0;
        this.totalExperience = 0;
        this.experienceProgress = 0.0f;
        ci.cancel();
    }
}
