package tallestred.piglinproliferation.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tallestred.piglinproliferation.PiglinProliferation;

public record AlchemistBeltSyncPacket(int slotId, ItemStack stack, int entityId) implements CustomPacketPayload {
    public static final Type<AlchemistBeltSyncPacket> TYPE = new Type<>(new ResourceLocation(PiglinProliferation.MODID, "alchemist_belt_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AlchemistBeltSyncPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, AlchemistBeltSyncPacket::slotId, ItemStack.STREAM_CODEC, AlchemistBeltSyncPacket::stack, ByteBufCodecs.INT, AlchemistBeltSyncPacket::entityId, AlchemistBeltSyncPacket::new);

    public static void handle(AlchemistBeltSyncPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerToClientPacketStuff.syncBelt(payload);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
