package aiefu.ebd.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void onGetUseAnimation(ItemStack stack, CallbackInfoReturnable<UseAnim> cir) {
        Item self = (Item) (Object) this;
        if (self == Items.GLISTERING_MELON_SLICE || self == Items.EXPERIENCE_BOTTLE) {
            cir.setReturnValue(UseAnim.DRINK);
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void onGetUseDuration(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        Item self = (Item) (Object) this;
        if (self == Items.GLISTERING_MELON_SLICE || self == Items.EXPERIENCE_BOTTLE) {
            cir.setReturnValue(32);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        Item self = (Item) (Object) this;
        if (self == Items.GLISTERING_MELON_SLICE || self == Items.EXPERIENCE_BOTTLE) {
            cir.setReturnValue(ItemUtils.startUsingInstantly(level, player, hand));
        }
    }

    @Inject(method = "components", at = @At("RETURN"), cancellable = true)
    private void onComponents(CallbackInfoReturnable<net.minecraft.core.component.DataComponentMap> cir) {
        Item self = (Item) (Object) this;
        if (self == Items.GLISTERING_MELON_SLICE) {
            net.minecraft.core.component.DataComponentMap original = cir.getReturnValue();
            net.minecraft.world.food.FoodProperties food = new net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(0)
                    .saturationModifier(0.0f)
                    .alwaysEdible()
                    .build();

            cir.setReturnValue(new net.minecraft.core.component.DataComponentMap() {
                @Override
                public <T> T get(net.minecraft.core.component.DataComponentType<? extends T> type) {
                    if (type == net.minecraft.core.component.DataComponents.FOOD) {
                        return (T) food;
                    }
                    return original.get(type);
                }

                @Override
                public boolean has(net.minecraft.core.component.DataComponentType<?> type) {
                    if (type == net.minecraft.core.component.DataComponents.FOOD) {
                        return true;
                    }
                    return original.has(type);
                }

                @Override
                public java.util.Set<net.minecraft.core.component.DataComponentType<?>> keySet() {
                    java.util.Set<net.minecraft.core.component.DataComponentType<?>> keys = new java.util.HashSet<>(original.keySet());
                    keys.add(net.minecraft.core.component.DataComponents.FOOD);
                    return keys;
                }
            });
        }
    }
}
