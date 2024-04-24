package tallestred.piglinproliferation.common.entities;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, PiglinProliferation.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<PiglinAlchemist>> PIGLIN_ALCHEMIST = ENTITIES.register("piglin_alchemist", () -> EntityType.Builder.of(PiglinAlchemist::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).setShouldReceiveVelocityUpdates(true).build(PiglinProliferation.MODID + "piglin_alchemist"));
    public static final DeferredHolder<EntityType<?>, EntityType<PiglinTraveler>> PIGLIN_TRAVELER = ENTITIES.register("piglin_traveler", () -> EntityType.Builder.of(PiglinTraveler::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).setShouldReceiveVelocityUpdates(true).build(PiglinProliferation.MODID + "piglin_traveler"));
}
