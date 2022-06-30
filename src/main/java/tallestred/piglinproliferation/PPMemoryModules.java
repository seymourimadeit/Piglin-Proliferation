package tallestred.piglinproliferation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPMemoryModules {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPE = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, PiglinProliferation.MODID);
    public static final RegistryObject<MemoryModuleType<Mob>> POTION_THROW_TARGET = MEMORY_MODULE_TYPE.register("potion_throw_target", () -> new MemoryModuleType<>(Optional.empty()));
}
