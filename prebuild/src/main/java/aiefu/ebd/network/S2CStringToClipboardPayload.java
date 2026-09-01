package aiefu.ebd.network;

import aiefu.ebd.EBDCommon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CStringToClipboardPayload(String text) implements CustomPacketPayload {
    public static final Type<S2CStringToClipboardPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "s2c_string_to_clipboard"));

    public static final StreamCodec<FriendlyByteBuf, S2CStringToClipboardPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeUtf(payload.text),
        buf -> new S2CStringToClipboardPayload(buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
