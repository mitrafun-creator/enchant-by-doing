package aiefu.ebd.network;

import aiefu.ebd.EBDCommon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CBoatSyncPayload(int entityId, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<S2CBoatSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "s2c_boat_sync"));

    public static final StreamCodec<FriendlyByteBuf, S2CBoatSyncPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeInt(payload.entityId);
            buf.writeNbt(payload.tag);
        },
        buf -> new S2CBoatSyncPayload(buf.readInt(), buf.readNbt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
