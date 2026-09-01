package aiefu.ebd.mixin;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.IServerPlayerAcc;
import aiefu.ebd.IGrindstoneMenu;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Механика обучения зачарованиям через точильный камень:
 * При снятии чар игрок ГАРАНТИРОВАННО (100%) выучивает одно из зачарований,
 * которые были на предмете и которые он ещё не изучил.
 * При успехе предмет в руке теряет 60-90% прочности.
 */
@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin implements IGrindstoneMenu {

    @Accessor("repairSlots")
    @Override
    public abstract Container ebd$getRepairSlots();

    @Inject(method = "quickMoveStack", at = @At("HEAD"))
    private void onBeforeQuickMove(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index != 2) return;
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        Container repairSlots = ebd$getRepairSlots();
        ItemStack input1 = repairSlots.getItem(0);
        ItemStack input2 = repairSlots.getItem(1);
        EBDCommon.handleGrindstoneDisenchant(serverPlayer, input1, input2);
    }
}
