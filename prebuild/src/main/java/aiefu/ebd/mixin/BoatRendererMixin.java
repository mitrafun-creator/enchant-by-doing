package aiefu.ebd.mixin;

import aiefu.ebd.IBoatEnchanted;
import aiefu.ebd.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BoatRenderer.class)
public class BoatRendererMixin {

    @Redirect(
        method = "render(Lnet/minecraft/world/entity/vehicle/Boat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
        )
    )
    private com.mojang.blaze3d.vertex.VertexConsumer ebd$getBoatBuffer(
        MultiBufferSource source,
        RenderType renderType,
        Boat boat
    ) {
        boolean hasEnchants = false;
        if (boat instanceof IBoatEnchanted enchanted) {
            hasEnchants = !enchanted.ebd$getBoatEnchantments().isEmpty();
        }
        if (hasEnchants) {
            return ItemRenderer.getFoilBufferDirect(source, renderType, false, true);
        }
        return source.getBuffer(renderType);
    }
}
