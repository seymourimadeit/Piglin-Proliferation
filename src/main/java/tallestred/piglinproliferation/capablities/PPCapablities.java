package tallestred.piglinproliferation.capablities;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.capabilities.CapabilityToken;
import net.neoforged.neoforge.common.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.LazyOptional;
import tallestred.piglinproliferation.PiglinProliferation;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPCapablities {
    public static final Capability<TransformationSourceListener> TRANSFORMATION_SOURCE_TRACKER = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<CriticalAura> GUARANTEED_CRIT_TRACKER = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(TransformationSourceListener.class);
        event.register(CriticalAura.class);
    }

    public static CriticalAura getGuaranteedCritical(LivingEntity entity) {
        LazyOptional<CriticalAura> listener = entity.getCapability(GUARANTEED_CRIT_TRACKER);
        if (listener.isPresent())
            return listener.orElseThrow(() -> new IllegalStateException("Capability not found! Report this to the Piglin Proliferation github!"));
        return null;
    }
}
