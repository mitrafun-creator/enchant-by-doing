package aiefu.ebd.mixin;

import aiefu.ebd.IBlockEntityEnchanted;
import aiefu.ebd.Utils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BarrelBlockEntity.class)
public abstract class BarrelBlockEntityMixin implements IBlockEntityEnchanted {

    @Shadow
    private NonNullList<ItemStack> items;

    @Shadow
    public abstract int getContainerSize();

    @Inject(method = "getContainerSize", at = @At("HEAD"), cancellable = true)
    private void ebd$getContainerSize(CallbackInfoReturnable<Integer> cir) {
        ItemEnchantments enc = this.ebd$getBlockEntityEnchantments();
        int capacityLvl = Utils.getEnchantmentLevel(enc, "capacity");
        if (capacityLvl > 0) {
            cir.setReturnValue(27 + 27 * capacityLvl);
        }
    }

    @Override
    public void ebd$onEnchantmentsChanged() {
        int expectedSize = this.getContainerSize();
        if (this.items.size() != expectedSize) {
            NonNullList<ItemStack> newItems = NonNullList.withSize(expectedSize, ItemStack.EMPTY);
            for (int i = 0; i < Math.min(this.items.size(), expectedSize); i++) {
                newItems.set(i, this.items.get(i));
            }
            this.items = newItems;
        }
    }

    @Inject(method = "createMenu", at = @At("HEAD"), cancellable = true)
    private void ebd$createMenu(int id, net.minecraft.world.entity.player.Inventory inventory, CallbackInfoReturnable<AbstractContainerMenu> cir) {
        int size = this.getContainerSize();
        if (size >= 54) {
            cir.setReturnValue(net.minecraft.world.inventory.ChestMenu.sixRows(id, inventory, (net.minecraft.world.Container) this));
        }
    }
}
