package tallestred.piglinproliferation.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public class CriticalCapabilityPacket {
    private final int entityId;
    private final boolean crit;

    public CriticalCapabilityPacket(int entityId, boolean crit) {
        this.entityId = entityId;
        this.crit = crit;
    }

    public static CriticalCapabilityPacket decode(FriendlyByteBuf buf) {
        return new CriticalCapabilityPacket(buf.readInt(), buf.readBoolean());
    }

    public static void encode(CriticalCapabilityPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.crit);
    }

    public void handle(NetworkEvent.ServerCustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerToClientPacketStuff.syncCritical(this);
        });
        context.setPacketHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public boolean getCrit() {
        return this.crit;
    }
}
