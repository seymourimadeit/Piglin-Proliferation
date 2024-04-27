package tallestred.piglinproliferation.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(PiglinProliferation.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void registerPackets() {
        int id = 0;
        INSTANCE.registerMessage(id++, ZiglinCapablitySyncPacket.class, ZiglinCapablitySyncPacket::encode, ZiglinCapablitySyncPacket::decode, ZiglinCapablitySyncPacket::handle);
        INSTANCE.registerMessage(id++, CriticalCapabilityPacket.class, CriticalCapabilityPacket::encode, CriticalCapabilityPacket::decode, CriticalCapabilityPacket::handle);
    }
}