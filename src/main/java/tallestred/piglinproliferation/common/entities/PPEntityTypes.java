package tallestred.piglinproliferation.common.entities;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPEntityTypes {
    public static DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, PiglinProliferation.MODID);
    public static final RegistryObject<EntityType<PiglinAlchemist>> PIGLIN_ALCHEMIST = ENTITIES.register("piglin_alchemist", () -> EntityType.Builder.of(PiglinAlchemist::new, MobCategory.MISC).sized(0.6F, 1.95F).setShouldReceiveVelocityUpdates(true).build(PiglinProliferation.MODID + "piglin_alchemist"));
}
