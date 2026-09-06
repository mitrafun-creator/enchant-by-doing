package aiefu.ebd.network;

import aiefu.ebd.EBDCommon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record S2CGlobalDataSyncPayload(int globalLevel, double globalXp, double neededGlobalXp, int skillPoints, Map<String, Integer> perks) implements CustomPacketPayload {
    public static final Type<S2CGlobalDataSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "s2c_global_data_sync"));

    public static final StreamCodec<FriendlyByteBuf, S2CGlobalDataSyncPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeInt(payload.globalLevel);
            buf.writeDouble(payload.globalXp);
            buf.writeDouble(payload.neededGlobalXp);
            buf.writeInt(payload.skillPoints);
            buf.writeVarInt(payload.perks.size());
            payload.perks.forEach((k, v) -> {
                buf.writeUtf(k);
                buf.writeInt(v);
            });
        },
        buf -> {
            int level = buf.readInt();
            double xp = buf.readDouble();
            double needed = buf.readDouble();
            int points = buf.readInt();
            int size = buf.readVarInt();
            Map<String, Integer> perks = new HashMap<>();
            for (int i = 0; i < size; i++) {
                perks.put(buf.readUtf(), buf.readInt());
            }
            return new S2CGlobalDataSyncPayload(level, xp, needed, points, perks);
        }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
