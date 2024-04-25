package tallestred.piglinproliferation.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tallestred.piglinproliferation.PiglinProliferation;

public record ZiglinCapabilitySyncPacket(int entityId, String transformedFromId) implements CustomPacketPayload {
    public static final Type<ZiglinCapabilitySyncPacket> TYPE = new Type<>(new ResourceLocation(PiglinProliferation.MODID, "transform_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ZiglinCapabilitySyncPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, ZiglinCapabilitySyncPacket::entityId, ByteBufCodecs.STRING_UTF8, ZiglinCapabilitySyncPacket::transformedFromId, ZiglinCapabilitySyncPacket::new);

    public static void handle(ZiglinCapabilitySyncPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ServerToClientPacketStuff.syncZiglinClothes(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}