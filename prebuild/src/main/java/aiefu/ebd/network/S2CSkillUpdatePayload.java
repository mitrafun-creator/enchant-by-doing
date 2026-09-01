package aiefu.ebd.network;

import aiefu.ebd.EBDCommon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CSkillUpdatePayload(String skillId, int level, double xp, double neededXp, double xpGained) implements CustomPacketPayload {
    public static final Type<S2CSkillUpdatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "s2c_skill_update"));

    public static final StreamCodec<FriendlyByteBuf, S2CSkillUpdatePayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeUtf(payload.skillId);
            buf.writeInt(payload.level);
            buf.writeDouble(payload.xp);
            buf.writeDouble(payload.neededXp);
            buf.writeDouble(payload.xpGained);
        },
        buf -> new S2CSkillUpdatePayload(
            buf.readUtf(),
            buf.readInt(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readDouble()
        )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
