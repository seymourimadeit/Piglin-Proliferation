package tallestred.piglinproliferation.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import tallestred.piglinproliferation.PPEvents;
import tallestred.piglinproliferation.capablities.CriticalAfterCharge;
import tallestred.piglinproliferation.capablities.PPCapablities;
import tallestred.piglinproliferation.capablities.TransformationSourceListener;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

public class ServerToClientPacketStuff {
    public static void syncBelt(AlchemistBeltSyncPacket msg) {
        Entity entity = Minecraft.getInstance().level.getEntity(msg.getEntityId());
        if (entity != null && entity instanceof PiglinAlchemist alchemist) {
            alchemist.setBeltInventorySlot(msg.slotId, msg.stack);
        }
    }

    public static void syncZiglinClothes(ZiglinCapablitySyncPacket msg) {
        Entity entity = Minecraft.getInstance().level.getEntity(msg.getEntityId());
        if (entity != null && entity instanceof ZombifiedPiglin ziglin) {
            TransformationSourceListener tSource = PPEvents.getTransformationSourceListener(ziglin);
            tSource.setTransformationSource(msg.getTransformedFromId());
        }
    }

    public static void syncCritical(CriticalCapabilityPacket msg) {
        Entity entity = Minecraft.getInstance().level.getEntity(msg.getEntityId());
        if (entity != null && entity instanceof LivingEntity living) {
            CriticalAfterCharge criticalAfterCharge = PPCapablities.getGuaranteedCritical(living);
            criticalAfterCharge.setCritical(msg.getCrit());
        }
    }
}
