package tallestred.piglinproliferation.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import tallestred.piglinproliferation.PiglinProliferation;

public class AlchemistBeltSyncPacket implements CustomPacketPayload {
    final int slotId;
    final ItemStack stack;
    private final int entityId;
    public static final ResourceLocation ID = new ResourceLocation(PiglinProliferation.MODID, "alchemist_belt_sync");

    public AlchemistBeltSyncPacket(int entityId, int slotID, ItemStack stack) {
        this.entityId = entityId;
        this.slotId = slotID;
        this.stack = stack;
    }

    public AlchemistBeltSyncPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.slotId = buf.readInt();
        this.stack = buf.readItem();
    }


    public void handle(PlayPayloadContext context) {
        context.workHandler().execute(() -> {
            ServerToClientPacketStuff.syncBelt(this);
        });
    }

    public int getEntityId() {
        return this.entityId;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.slotId);
        buf.writeItem(this.stack);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
