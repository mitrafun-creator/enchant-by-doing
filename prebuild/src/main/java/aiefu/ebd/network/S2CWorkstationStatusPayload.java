package aiefu.ebd.network;

import aiefu.ebd.EBDCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CWorkstationStatusPayload(byte nearbyMask, String missingWorkstationId) implements CustomPacketPayload {
    public static final Type<S2CWorkstationStatusPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "workstation_status"));

    public static final StreamCodec<ByteBuf, S2CWorkstationStatusPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE,
        S2CWorkstationStatusPayload::nearbyMask,
        ByteBufCodecs.STRING_UTF8,
        S2CWorkstationStatusPayload::missingWorkstationId,
        S2CWorkstationStatusPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}