package tallestred.piglinproliferation.networking;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import tallestred.piglinproliferation.PiglinProliferation;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class PPNetworking {
    private static final Supplier<String> PROTOCOL_VERSION = () -> "client_sync";
    private static final Predicate<String> VERSION_PREDICATE = (String s) -> PROTOCOL_VERSION.get().equals(s);
    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.named(ResourceLocation.tryParse(PiglinProliferation.MODID)).networkProtocolVersion(PROTOCOL_VERSION).clientAcceptedVersions(VERSION_PREDICATE).serverAcceptedVersions(VERSION_PREDICATE).simpleChannel();

    public static void registerPackets() {
        int i = 0;
        //TODO NOT SURE IF THESE SHOULD ALL ACTUALLY BE SERVER CUSTOM PAYLOAD!!
        INSTANCE.messageBuilder(AlchemistBeltSyncPacket.class, i++).encoder(AlchemistBeltSyncPacket::encode).decoder(AlchemistBeltSyncPacket::decode).consumerMainThread(AlchemistBeltSyncPacket::handle).add();
        INSTANCE.messageBuilder(ZiglinCapablitySyncPacket.class, i++).encoder(ZiglinCapablitySyncPacket::encode).decoder(ZiglinCapablitySyncPacket::decode).consumerMainThread(ZiglinCapablitySyncPacket::handle).add();
        INSTANCE.messageBuilder(CriticalCapabilityPacket.class, i++).encoder(CriticalCapabilityPacket::encode).decoder(CriticalCapabilityPacket::decode).consumerMainThread(CriticalCapabilityPacket::handle).add();
    }
}
