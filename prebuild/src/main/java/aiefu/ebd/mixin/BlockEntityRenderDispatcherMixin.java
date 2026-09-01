package aiefu.ebd.mixin;

import aiefu.ebd.IBlockEntityEnchanted;
import aiefu.ebd.client.EnchantedBlockRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Shadow
    private Map<net.minecraft.world.level.block.entity.BlockEntityType<?>, BlockEntityRenderer<?>> renderers;

    @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void ebd$fallbackRenderer(E blockEntity, CallbackInfoReturnable<BlockEntityRenderer<E>> cir) {
        if (blockEntity instanceof IBlockEntityEnchanted enchanted && !enchanted.ebd$getBlockEntityEnchantments().isEmpty()) {
            BlockEntityRenderer<E> original = (BlockEntityRenderer<E>) this.renderers.get(blockEntity.getType());
            if (original == null) {
                cir.setReturnValue((BlockEntityRenderer<E>) EnchantedBlockRenderer.INSTANCE);
            }
        }
    }

    @Redirect(
        method = "setupAndRender",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
        )
    )
    private static <T extends BlockEntity> void ebd$wrapBlockEntityRenderer(
        BlockEntityRenderer<T> renderer, T blockEntity, float partialTicks, com.mojang.blaze3d.vertex.PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay
    ) {
        MultiBufferSource sourceToUse = bufferSource;
        if (blockEntity instanceof IBlockEntityEnchanted enchanted && !enchanted.ebd$getBlockEntityEnchantments().isEmpty()) {
            boolean isBlockModel = renderer instanceof EnchantedBlockRenderer;
            sourceToUse = (renderType) -> {
                String name = renderType.toString();
                if (name.contains("outline") || name.contains("stage") || name.contains("shadow") || name.contains("cracks")) {
                    return bufferSource.getBuffer(renderType);
                }
                return net.minecraft.client.renderer.entity.ItemRenderer.getFoilBufferDirect(bufferSource, renderType, isBlockModel, true);
            };
        }
        renderer.render(blockEntity, partialTicks, poseStack, sourceToUse, packedLight, packedOverlay);
    }
}
