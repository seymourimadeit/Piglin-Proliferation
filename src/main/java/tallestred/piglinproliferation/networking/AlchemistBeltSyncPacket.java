package tallestred.piglinproliferation.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import java.util.function.Supplier;

public class AlchemistBeltSyncPacket {
    private final int entityId;
    private final int slotId;
    private final ItemStack stack;

    public AlchemistBeltSyncPacket(int entityId, int slotID, ItemStack stack) {
        this.entityId = entityId;
        this.slotId = slotID;
        this.stack = stack;
    }

    public static AlchemistBeltSyncPacket decode(FriendlyByteBuf buf) {
        return new AlchemistBeltSyncPacket(buf.readVarInt(), buf.readInt(), buf.readItem());
    }

    public static void encode(AlchemistBeltSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeInt(msg.slotId);
        buf.writeItem(msg.stack);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public static void handle(AlchemistBeltSyncPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().player.level.getEntity(msg.getEntityId());
            if (entity != null && entity instanceof PiglinAlchemist alchemist) {
                alchemist.setBeltInventorySlot(msg.slotId, msg.stack);
            }
        });
        context.get().setPacketHandled(true);
    }
}
