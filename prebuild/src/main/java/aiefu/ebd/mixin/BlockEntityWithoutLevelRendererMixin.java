package aiefu.ebd.mixin;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public class BlockEntityWithoutLevelRendererMixin {

    @ModifyVariable(method = "renderByItem", at = @At("HEAD"), argsOnly = true)
    private MultiBufferSource ebd$wrapItemBufferSource(MultiBufferSource bufferSource, ItemStack stack) {
        if (!stack.isEmpty() && stack.hasFoil()) {
            return (renderType) -> {
                String name = renderType.toString();
                if (name.contains("outline") || name.contains("stage") || name.contains("shadow") || name.contains("cracks")) {
                    return bufferSource.getBuffer(renderType);
                }
                return net.minecraft.client.renderer.entity.ItemRenderer.getFoilBufferDirect(bufferSource, renderType, true, true);
            };
        }
        return bufferSource;
    }
}
