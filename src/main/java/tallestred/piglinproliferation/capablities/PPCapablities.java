package tallestred.piglinproliferation.capablities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestred.piglinproliferation.PiglinProliferation;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPCapablities {
    public static final Capability<TransformationSourceListener> TRANSFORMATION_SOURCE_TRACKER = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<CriticalAfterCharge> GUARANTEED_CRIT_TRACKER = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(TransformationSourceListener.class);
    }

    public static CriticalAfterCharge getGuaranteedCritical(LivingEntity entity) {
        LazyOptional<CriticalAfterCharge> listener = entity.getCapability(GUARANTEED_CRIT_TRACKER);
        if (listener.isPresent())
            return listener.orElseThrow(() -> new IllegalStateException("Capability not found! Report this to the Big Brain github!"));
        return null;
    }
}
