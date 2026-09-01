package aiefu.ebd.network;

import aiefu.ebd.EBDCommon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CDataSyncPayload(byte[] bytes) implements CustomPacketPayload {
    public static final Type<S2CDataSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "s2c_data_sync"));

    public static final StreamCodec<FriendlyByteBuf, S2CDataSyncPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeByteArray(payload.bytes),
        buf -> new S2CDataSyncPayload(buf.readByteArray())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
