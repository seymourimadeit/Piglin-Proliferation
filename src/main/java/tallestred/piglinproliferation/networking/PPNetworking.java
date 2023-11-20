package tallestred.piglinproliferation.networking;

import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPNetworking {
    private static final Integer PROTOCOL_VERSION = 1;
    public static final SimpleChannel INSTANCE = ChannelBuilder.named(PiglinProliferation.MODID).networkProtocolVersion(PROTOCOL_VERSION).clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION)).serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION)).simpleChannel();

    public static void registerPackets() {
        int i = 0;
        INSTANCE.messageBuilder(AlchemistBeltSyncPacket.class, i++).encoder(AlchemistBeltSyncPacket::encode).decoder(AlchemistBeltSyncPacket::decode).consumerMainThread(AlchemistBeltSyncPacket::handle).add();
        INSTANCE.messageBuilder(ZiglinCapablitySyncPacket.class, i++).encoder(ZiglinCapablitySyncPacket::encode).decoder(ZiglinCapablitySyncPacket::decode).consumerMainThread(ZiglinCapablitySyncPacket::handle).add();
        INSTANCE.messageBuilder(CriticalCapabilityPacket.class, i++).encoder(CriticalCapabilityPacket::encode).decoder(CriticalCapabilityPacket::decode).consumerMainThread(CriticalCapabilityPacket::handle).add();
    }
}
