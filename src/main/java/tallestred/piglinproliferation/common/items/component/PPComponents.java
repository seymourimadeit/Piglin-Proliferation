package tallestred.piglinproliferation.common.items.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.component.LodestoneTracker;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPComponents {
    public static DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PiglinProliferation.MODID);
    public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BUCKLER_CHARGE_TICKS = COMPONENTS.register("buckler_charge_ticks", () -> DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).build());
    public static DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BUCKLER_IS_READY = COMPONENTS.register("buckler_is_ready", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TravelersCompassTracker>> TRAVELERS_COMPASS_TRACKER = COMPONENTS.register("travelers_compass_tracker", () -> DataComponentType.<TravelersCompassTracker>builder().persistent(TravelersCompassTracker.CODEC).networkSynchronized(TravelersCompassTracker.STREAM_CODEC).cacheEncoding().build());
}
