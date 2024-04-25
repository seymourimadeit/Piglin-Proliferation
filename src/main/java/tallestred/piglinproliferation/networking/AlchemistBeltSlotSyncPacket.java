package tallestred.piglinproliferation.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tallestred.piglinproliferation.PiglinProliferation;

import java.util.List;

public record AlchemistBeltSlotSyncPacket(int slotId, ItemStack stack, int entityId) implements CustomPacketPayload {
    public static final Type<AlchemistBeltSlotSyncPacket> TYPE = new Type<>(new ResourceLocation(PiglinProliferation.MODID, "alchemist_belt_slot_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AlchemistBeltSlotSyncPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, AlchemistBeltSlotSyncPacket::slotId, ItemStack.OPTIONAL_STREAM_CODEC, AlchemistBeltSlotSyncPacket::stack, ByteBufCodecs.INT, AlchemistBeltSlotSyncPacket::entityId, AlchemistBeltSlotSyncPacket::new);

    public static void handle(AlchemistBeltSlotSyncPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ServerToClientPacketStuff.syncBeltSlot(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
