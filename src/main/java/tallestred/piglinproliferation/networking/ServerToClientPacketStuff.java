package tallestred.piglinproliferation.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.level.Level;
import tallestred.piglinproliferation.capablities.PPCapabilities;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import javax.annotation.Nullable;

public class ServerToClientPacketStuff {
    public static void syncBelt(AlchemistBeltSyncPacket msg) {
        if (getEntity(msg.getEntityId()) instanceof PiglinAlchemist alchemist) {
            alchemist.setBeltInventorySlot(msg.slotId, msg.stack);
        }
    }

    public static void syncZiglinClothes(ZiglinCapablitySyncPacket msg) {
        if (getEntity(msg.getEntityId()) instanceof ZombifiedPiglin ziglin) {
            ziglin.setData(PPCapabilities.TRANSFORMATION_TRACKER.get(), msg.getTransformedFromId());
        }
    }

    public static void syncCritical(CriticalCapabilityPacket msg) {
        if (getEntity(msg.getEntityId()) instanceof LivingEntity living) {
            living.setData(PPCapabilities.CRITICAL.get(), msg.getCrit());
        }
    }

    @Nullable
    public static Entity getEntity(int entityID) {
        Level level = Minecraft.getInstance().level;
        return level != null ? level.getEntity(entityID) : null;
    }
}
