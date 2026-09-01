package aiefu.ebd.network;

import aiefu.ebd.EBDCommon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CMatConfigSyncPayload(byte[] bytes) implements CustomPacketPayload {
    public static final Type<S2CMatConfigSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "s2c_mat_config_sync"));

    public static final StreamCodec<FriendlyByteBuf, S2CMatConfigSyncPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeByteArray(payload.bytes),
        buf -> new S2CMatConfigSyncPayload(buf.readByteArray())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
