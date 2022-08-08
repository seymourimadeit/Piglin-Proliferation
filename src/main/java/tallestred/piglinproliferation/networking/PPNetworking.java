package tallestred.piglinproliferation.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import tallestred.piglinproliferation.PPEvents;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.capablities.TransformationSourceListener;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

public class PPNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(PiglinProliferation.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void registerPackets() {
        int id = 0;
        INSTANCE.registerMessage(id++, AlchemistBeltSyncPacket.class, AlchemistBeltSyncPacket::encode, AlchemistBeltSyncPacket::decode, AlchemistBeltSyncPacket::handle);
        INSTANCE.registerMessage(id++, ZiglinCapablitySyncPacket.class, ZiglinCapablitySyncPacket::encode, ZiglinCapablitySyncPacket::decode, ZiglinCapablitySyncPacket::handle);
    }

    @OnlyIn(Dist.CLIENT)
    public static void syncBelt(AlchemistBeltSyncPacket msg) {
        Entity entity = Minecraft.getInstance().level.getEntity(msg.getEntityId());
        if (entity != null && entity instanceof PiglinAlchemist alchemist) {
            alchemist.setBeltInventorySlot(msg.slotId, msg.stack);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void syncZiglinClothes(ZiglinCapablitySyncPacket msg) {
        Entity entity = Minecraft.getInstance().level.getEntity(msg.getEntityId());
        if (entity != null && entity instanceof ZombifiedPiglin ziglin) {
            TransformationSourceListener tSource = PPEvents.getTransformationSourceListener(ziglin);
            tSource.setTransformationSource(msg.getTransformedFromId());
        }
    }
}
