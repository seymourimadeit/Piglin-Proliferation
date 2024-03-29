package tallestred.piglinproliferation.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;

public class AlchemistBeltSyncPacket {
    final int slotId;
    final ItemStack stack;
    private final int entityId;

    public AlchemistBeltSyncPacket(int entityId, int slotID, ItemStack stack) {
        this.entityId = entityId;
        this.slotId = slotID;
        this.stack = stack;
    }

    public static AlchemistBeltSyncPacket decode(FriendlyByteBuf buf) {
        return new AlchemistBeltSyncPacket(buf.readInt(), buf.readInt(), buf.readItem());
    }

    public static void encode(AlchemistBeltSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeInt(msg.slotId);
        buf.writeItem(msg.stack);
    }

    public void handle(NetworkEvent.ServerCustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerToClientPacketStuff.syncBelt(this);
        });
        context.setPacketHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }
}
