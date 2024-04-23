package tallestred.piglinproliferation.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import tallestred.piglinproliferation.capablities.CriticalAura;
import tallestred.piglinproliferation.capablities.PPCapablities;
import tallestred.piglinproliferation.capablities.TransformationSourceListener;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

public class ServerToClientPacketStuff {
    public static void syncBelt(AlchemistBeltSyncPacket msg) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(msg.getEntityId()) instanceof PiglinAlchemist alchemist) {
            alchemist.setBeltInventorySlot(msg.slotId, msg.stack);
        }
    }

    public static void syncZiglinClothes(ZiglinCapablitySyncPacket msg) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(msg.getEntityId()) instanceof ZombifiedPiglin ziglin) {
            TransformationSourceListener tSource = TransformationSourceListener.from(ziglin);
            tSource.setTransformationSource(msg.getTransformedFromId());
        }
    }

    public static void syncCritical(CriticalCapabilityPacket msg) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(msg.getEntityId()) instanceof LivingEntity living) {
            CriticalAura criticalAura = PPCapablities.getGuaranteedCritical(living);
            criticalAura.setCritical(msg.getCrit());
        }
    }
}
