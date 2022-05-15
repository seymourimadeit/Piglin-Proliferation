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
        INSTANCE.registerMessage(id++, AlchemistBeltSyncPacket.class, AlchemistBeltSyncPacket::encode, AlchemistBeltSyncPacket::decode, AlchemistBeltSyncPacket::handle);
    }
}
