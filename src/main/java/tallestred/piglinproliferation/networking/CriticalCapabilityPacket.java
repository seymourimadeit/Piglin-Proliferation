package tallestred.piglinproliferation.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tallestred.piglinproliferation.PiglinProliferation;

public record CriticalCapabilityPacket(int entityId, boolean crit) implements CustomPacketPayload {
    public static final Type<CriticalCapabilityPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PiglinProliferation.MODID, "crit_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CriticalCapabilityPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, CriticalCapabilityPacket::entityId, ByteBufCodecs.BOOL, CriticalCapabilityPacket::crit, CriticalCapabilityPacket::new);

    public static void handle(CriticalCapabilityPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ServerToClientPacketStuff.syncCritical(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
