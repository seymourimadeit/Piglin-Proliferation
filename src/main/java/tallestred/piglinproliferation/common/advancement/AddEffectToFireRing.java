package tallestred.piglinproliferation.common.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class AddEffectToFireRing extends SimpleCriterionTrigger<AddEffectToFireRing.TriggerInstance> {

    public AddEffectToFireRing() {
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, (triggerInstance) -> true);
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create((triggerInstanceInstance) -> {
            return triggerInstanceInstance.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player)).apply(triggerInstanceInstance, TriggerInstance::new);
        });

        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}
