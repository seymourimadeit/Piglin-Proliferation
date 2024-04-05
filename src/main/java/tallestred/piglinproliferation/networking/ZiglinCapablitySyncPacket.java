package tallestred.piglinproliferation.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import tallestred.piglinproliferation.PiglinProliferation;

public class ZiglinCapablitySyncPacket implements CustomPacketPayload {
    private final int entityId;
    private final String transformedFromId;

    public static final ResourceLocation ID = new ResourceLocation(PiglinProliferation.MODID, "transform_sync");

    public ZiglinCapablitySyncPacket(int entityId, String transformedFromId) {
        this.entityId = entityId;
        this.transformedFromId = transformedFromId;
    }

    public ZiglinCapablitySyncPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.transformedFromId = buf.readUtf();
    }

    @Override
    public void write( FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeUtf(this.transformedFromId);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext context) {
        context.workHandler().execute(() -> {
            ServerToClientPacketStuff.syncZiglinClothes(this);
        });
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getTransformedFromId() {
        return this.transformedFromId;
    }
}
