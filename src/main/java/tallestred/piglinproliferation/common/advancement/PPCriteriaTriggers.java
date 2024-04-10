package tallestred.piglinproliferation.common.advancement;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPCriteriaTriggers {
    public static DeferredRegister<CriterionTrigger<?>> CRITERIA_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, PiglinProliferation.MODID);
    public static DeferredHolder<CriterionTrigger<?>, AddEffectToFireRing> ADD_EFFECT_TO_FIRE_RING = CRITERIA_TRIGGERS.register("add_effect_to_fire_ring", AddEffectToFireRing::new);
}
