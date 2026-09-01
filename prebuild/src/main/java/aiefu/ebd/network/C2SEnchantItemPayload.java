package aiefu.ebd.network;

import aiefu.ebd.EBDCommon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SEnchantItemPayload(String enchantmentId, int ordinal) implements CustomPacketPayload {
    public static final Type<C2SEnchantItemPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "c2s_enchant_item"));

    public static final StreamCodec<FriendlyByteBuf, C2SEnchantItemPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeUtf(payload.enchantmentId);
            buf.writeVarInt(payload.ordinal);
        },
        buf -> new C2SEnchantItemPayload(buf.readUtf(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
