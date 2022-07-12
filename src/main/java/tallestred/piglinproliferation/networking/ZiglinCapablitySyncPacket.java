package tallestred.piglinproliferation.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraftforge.network.NetworkEvent;
import tallestred.piglinproliferation.PPEvents;
import tallestred.piglinproliferation.capablities.TransformationSourceListener;

import java.util.function.Supplier;

public class ZiglinCapablitySyncPacket {
    private final int entityId;
    private final String transformedFromId;

    public ZiglinCapablitySyncPacket(int entityId, String transformedFromId) {
        this.entityId = entityId;
        this.transformedFromId = transformedFromId;
    }

    public static ZiglinCapablitySyncPacket decode(FriendlyByteBuf buf) {
        return new ZiglinCapablitySyncPacket(buf.readVarInt(), buf.readUtf());
    }

    public static void encode(ZiglinCapablitySyncPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeUtf(msg.transformedFromId);
    }

    public static void handle(ZiglinCapablitySyncPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().player.level.getEntity(msg.getEntityId());
            if (entity != null && entity instanceof ZombifiedPiglin ziglin) {
                TransformationSourceListener tSource = PPEvents.getTransformationSourceListener(ziglin);
                tSource.setTransformationSource(msg.getTransformedFromId());
            }
        });
        context.get().setPacketHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getTransformedFromId() {
        return this.transformedFromId;
    }
}
