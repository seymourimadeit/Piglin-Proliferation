package tallestred.piglinproliferation.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import tallestred.piglinproliferation.PiglinProliferation;

public class CriticalCapabilityPacket implements CustomPacketPayload {
    private final int entityId;
    private final boolean crit;
    public static final ResourceLocation ID = new ResourceLocation(PiglinProliferation.MODID, "crit_sync");

    public CriticalCapabilityPacket(int entityId, boolean crit) {
        this.entityId = entityId;
        this.crit = crit;
    }

    public CriticalCapabilityPacket(FriendlyByteBuf buf) {
        this.crit = buf.readBoolean();
        this.entityId = buf.readInt();
    }


    public void handle(PlayPayloadContext context) {
        context.workHandler().execute(() -> {
            ServerToClientPacketStuff.syncCritical(this);
        });
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeBoolean(this.crit);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public boolean getCrit() {
        return this.crit;
    }
}
