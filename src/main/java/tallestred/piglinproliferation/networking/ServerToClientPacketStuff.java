package tallestred.piglinproliferation.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.level.Level;
import tallestred.piglinproliferation.capablities.PPDataAttachments;

import javax.annotation.Nullable;

public class ServerToClientPacketStuff {
    public static void syncZiglinClothes(ZiglinCapabilitySyncPacket msg) {
        if (getEntity(msg.entityId()) instanceof ZombifiedPiglin ziglin)
            ziglin.setData(PPDataAttachments.TRANSFORMATION_TRACKER.get(), msg.transformedFromId());
    }

    public static void syncCritical(CriticalCapabilityPacket msg) {
        if (getEntity(msg.entityId()) instanceof LivingEntity living)
            living.setData(PPDataAttachments.CRITICAL.get(), msg.crit());
    }

    @Nullable
    public static Entity getEntity(int entityID) {
        Level level = Minecraft.getInstance().level;
        return level != null ? level.getEntity(entityID) : null;
    }
}
