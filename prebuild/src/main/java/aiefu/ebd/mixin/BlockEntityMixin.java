package aiefu.ebd.mixin;

import aiefu.ebd.IBlockEntityEnchanted;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements IBlockEntityEnchanted {

    @Unique
    private ItemEnchantments ebd$enchantments = ItemEnchantments.EMPTY;

    @Override
    public ItemEnchantments ebd$getBlockEntityEnchantments() {
        return ebd$enchantments;
    }

    @Override
    public void ebd$setBlockEntityEnchantments(ItemEnchantments enchantments) {
        this.ebd$enchantments = enchantments == null ? ItemEnchantments.EMPTY : enchantments;
        this.ebd$onEnchantmentsChanged();
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void ebd$saveAdditional(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (ebd$enchantments != null && !ebd$enchantments.isEmpty()) {
            net.minecraft.resources.RegistryOps<net.minecraft.nbt.Tag> ops =
                    net.minecraft.resources.RegistryOps.create(NbtOps.INSTANCE, provider);
            ItemEnchantments.CODEC.encodeStart(ops, ebd$enchantments)
                    .result()
                    .ifPresent(nbt -> tag.put("ESOEnch", nbt));
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void ebd$loadAdditional(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (tag.contains("ESOEnch")) {
            net.minecraft.resources.RegistryOps<net.minecraft.nbt.Tag> ops =
                    net.minecraft.resources.RegistryOps.create(NbtOps.INSTANCE, provider);
            ItemEnchantments.CODEC.parse(ops, tag.get("ESOEnch"))
                    .result()
                    .ifPresent(this::ebd$setBlockEntityEnchantments);
        }
    }

    @Inject(method = "getUpdateTag", at = @At("RETURN"), cancellable = true)
    private void ebd$getUpdateTag(HolderLookup.Provider provider, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        if (ebd$enchantments != null && !ebd$enchantments.isEmpty() && tag != null) {
            net.minecraft.resources.RegistryOps<net.minecraft.nbt.Tag> ops =
                    net.minecraft.resources.RegistryOps.create(NbtOps.INSTANCE, provider);
            ItemEnchantments.CODEC.encodeStart(ops, ebd$enchantments)
                    .result()
                    .ifPresent(nbt -> tag.put("ESOEnch", nbt));
        }
    }

    @Inject(method = "getUpdatePacket", at = @At("HEAD"), cancellable = true)
    private void ebd$getUpdatePacket(CallbackInfoReturnable<net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket> cir) {
        cir.setReturnValue(net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create((BlockEntity)(Object)this));
    }
}
