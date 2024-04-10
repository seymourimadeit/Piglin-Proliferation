package tallestred.piglinproliferation.common.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import tallestred.piglinproliferation.PiglinProliferation;

public class AddEffectToFireRing extends SimpleCriterionTrigger<AddEffectToFireRing.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation(PiglinProliferation.MODID, "add_effect_to_fire_ring");

    public AddEffectToFireRing() {
    }

    public TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
        return new TriggerInstance(predicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, (predicate) -> true);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(ContextAwarePredicate predicate) {
            super(ID, predicate);
        }
    }
}