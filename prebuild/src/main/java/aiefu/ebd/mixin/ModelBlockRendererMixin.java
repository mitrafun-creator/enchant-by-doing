package aiefu.ebd.mixin;

import aiefu.ebd.IBlockEntityEnchanted;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {

    @Inject(method = "tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V", at = @At("HEAD"), cancellable = true)
    private void ebd$hideEnchantedStaticBlock(net.minecraft.world.level.BlockAndTintGetter level, net.minecraft.client.resources.model.BakedModel model, BlockState state, BlockPos pos, com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, boolean checkSides, net.minecraft.util.RandomSource random, long seed, int packedOverlay, net.neoforged.neoforge.client.model.data.ModelData modelData, net.minecraft.client.renderer.RenderType renderType, CallbackInfo ci) {
        if (aiefu.ebd.client.EnchantedBlockRenderer.IS_RENDERING.get()) {
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IBlockEntityEnchanted enchanted && !enchanted.ebd$getBlockEntityEnchantments().isEmpty()) {
            ci.cancel();
        }
    }
}
