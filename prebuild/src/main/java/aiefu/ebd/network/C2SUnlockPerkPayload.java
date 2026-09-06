package aiefu.ebd.network;

import aiefu.ebd.EBDCommon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SUnlockPerkPayload(String perkId) implements CustomPacketPayload {
    public static final Type<C2SUnlockPerkPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "c2s_unlock_perk"));

    public static final StreamCodec<FriendlyByteBuf, C2SUnlockPerkPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeUtf(payload.perkId),
        buf -> new C2SUnlockPerkPayload(buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
