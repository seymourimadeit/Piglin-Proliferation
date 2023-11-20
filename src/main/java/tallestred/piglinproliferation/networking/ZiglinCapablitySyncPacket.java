package tallestred.piglinproliferation.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class ZiglinCapablitySyncPacket {
    private final int entityId;
    private final String transformedFromId;

    public ZiglinCapablitySyncPacket(int entityId, String transformedFromId) {
        this.entityId = entityId;
        this.transformedFromId = transformedFromId;
    }

    public static ZiglinCapablitySyncPacket decode(FriendlyByteBuf buf) {
        return new ZiglinCapablitySyncPacket(buf.readInt(), buf.readUtf());
    }

    public static void encode(ZiglinCapablitySyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeUtf(msg.transformedFromId);
    }

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerToClientPacketStuff.syncZiglinClothes(this);
        });
        context.setPacketHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getTransformedFromId() {
        return this.transformedFromId;
    }
}
