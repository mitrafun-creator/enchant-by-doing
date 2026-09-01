package aiefu.ebd.client.render;

import aiefu.ebd.entity.ThrownAxeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ThrownAxeRenderer extends EntityRenderer<ThrownAxeEntity> {
    private final ItemRenderer itemRenderer;

    public ThrownAxeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ThrownAxeEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Align to facing direction (yaw)
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        // Tilt for pitch
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F));

        float age = (float) entity.tickCount + partialTicks;

        if (entity.isNoPhysics() && !entity.isAxeInGround()) {
            // Возврат — быстрое вращение (50 deg/tick)
            poseStack.mulPose(Axis.ZP.rotationDegrees(age * 50.0F));
        } else if (!entity.isAxeInGround()) {
            // Полёт вперёд — нормальное вращение (25 deg/tick)
            poseStack.mulPose(Axis.ZP.rotationDegrees(age * 25.0F));
        } else {
            // Воткнулся в землю — фиксированный угол
            poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
        }

        poseStack.scale(1.2F, 1.2F, 1.2F);

        ItemStack stack = entity.getAxeItem();
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownAxeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
