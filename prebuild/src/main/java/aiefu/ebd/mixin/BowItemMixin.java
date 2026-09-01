package aiefu.ebd.mixin;

import aiefu.ebd.Utils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BowItem.class)
public class BowItemMixin {
    @ModifyVariable(
        method = "releaseUsing",
        at = @At("HEAD"),
        argsOnly = true
    )
    private int modifyTimeLeft(int timeLeft, ItemStack stack, net.minecraft.world.level.Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            int hunterLevel = Utils.getSkillLevel(player, "hunter");
            if (hunterLevel >= 50) {
                double speedMultiplier = 1.25;
                if (hunterLevel >= 100) {
                    speedMultiplier = 1.50;
                }
                int useDuration = ((BowItem) (Object) this).getUseDuration(stack, entity);
                int actualTicksUsed = useDuration - timeLeft;
                int modifiedTicksUsed = (int) (actualTicksUsed * speedMultiplier);
                return Math.max(0, useDuration - modifiedTicksUsed);
            }
        }
        return timeLeft;
    }
}
