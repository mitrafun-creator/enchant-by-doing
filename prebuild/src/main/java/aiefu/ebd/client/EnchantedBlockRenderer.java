package aiefu.ebd.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantedBlockRenderer implements BlockEntityRenderer<BlockEntity> {
    public static final EnchantedBlockRenderer INSTANCE = new EnchantedBlockRenderer();
    public static final ThreadLocal<Boolean> IS_RENDERING = ThreadLocal.withInitial(() -> false);

    private EnchantedBlockRenderer() {}

    @Override
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        try {
            IS_RENDERING.set(true);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            IS_RENDERING.remove();
        }
    }
}
