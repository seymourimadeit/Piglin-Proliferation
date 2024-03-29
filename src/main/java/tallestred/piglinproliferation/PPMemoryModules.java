package tallestred.piglinproliferation;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class PPMemoryModules {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPE = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, PiglinProliferation.MODID);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Mob>> POTION_THROW_TARGET = MEMORY_MODULE_TYPE.register("potion_throw_target", () -> new MemoryModuleType<>(Optional.empty()));
}
