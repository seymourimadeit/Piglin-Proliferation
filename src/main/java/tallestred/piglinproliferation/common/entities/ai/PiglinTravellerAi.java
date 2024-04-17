package tallestred.piglinproliferation.common.entities.ai;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.piglin.*;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;
import tallestred.piglinproliferation.common.entities.ai.behaviors.*;
import tallestred.piglinproliferation.common.items.TravellersCompassItem;
import tallestred.piglinproliferation.common.loot.PPLoot;

import java.util.List;
import java.util.Optional;

import static tallestred.piglinproliferation.util.CodeUtilities.castElementsToList;

public class PiglinTravellerAi extends AbstractPiglinAi<PiglinTraveller> {
    public static PiglinTravellerAi INSTANCE = new PiglinTravellerAi();

    public PiglinTravellerAi() {
    }

    @Override
    protected List<BehaviorControl<? super PiglinTraveller>> coreBehaviors(PiglinTraveller piglin) {
        return castElementsToList(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                InteractWithDoor.create(),
                new SwimOnlyOutOfLava(0.8F),
                StopHoldingItemAfterAdmiring.create(this, PPLoot.TRAVELLER_BARTER/*, PPLoot.TRAVELLER_BARTER_CHEAP, PPLoot.TRAVELLER_BARTER_EXPENSIVE*/),
                StartAdmiringItemIfSeen.create(120),
                StartCelebratingIfTargetDead.create(300, PiglinAi::wantsToDance),
                StopBeingAngryIfTargetDead.create()
        );
    }

    @Override
    protected List<BehaviorControl<? super PiglinTraveller>> idleBehaviors(PiglinTraveller piglin) {
        return castElementsToList(
                SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
                StartAttacking.create(AbstractPiglin::isAdult, this::nearestValidAttackTarget),
                BehaviorBuilder.triggerIf(Piglin::canHunt, StartHuntingHoglin.create()),
                PiglinAi.babySometimesRideBabyHoglin(),
                PiglinAi.createIdleLookBehaviors(),
                PiglinAi.createIdleMovementBehaviors(),
                SetLookAndInteract.create(EntityType.PLAYER, 4)
        );
    }

    @Override
    protected List<BehaviorControl<? super PiglinTraveller>> fightBehaviors(PiglinTraveller piglin) {
        var list = super.fightBehaviors(piglin);
        list.add(KickWhenClose.create(2));
        return list;
    }

    @Override
    protected Optional<? extends LivingEntity> nearestValidAttackTarget(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        if (isNearZombified(piglin)) {
            return Optional.empty();
        } else {
            var optional = BehaviorUtils.getLivingEntityFromUUIDMemory(piglin, MemoryModuleType.ANGRY_AT);
            if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(piglin, optional.get()))
                return optional;
            else {
                var fallbackOptional = findNemesisTarget(brain);
                if (fallbackOptional.isPresent())
                    return fallbackOptional;
            }
        }
        return Optional.empty();
    }



    @Override
    public void specificThrowItemBehaviour(PiglinTraveller piglin, ItemStack stack) {
        if (stack.getItem() instanceof TravellersCompassItem) {
            piglin.playBarteringAnimation();
            piglin.level().playSound(null, piglin.getX(), piglin.getY(), piglin.getZ(), PPSounds.MAKE_COMPASS.get(), piglin.getSoundSource(), 1.0F, 1.0F);
        }
    }

    @Override
    public SoundEvent soundForActivity(PiglinTraveller piglin, Activity activity) {
        if (activity == Activity.FIGHT) {
            return PPSounds.TRAVELLER_ANGRY.get();
        } else if (piglin.isConverting()) {
            return PPSounds.TRAVELLER_RETREAT.get();
        } else if (activity == Activity.AVOID && isNearAvoidTarget(piglin)) {
            return PPSounds.TRAVELLER_RETREAT.get();
        } else if (activity == Activity.ADMIRE_ITEM) {
            return PPSounds.TRAVELLER_ADMIRE.get();
        } else if (activity == Activity.CELEBRATE) {
            return PPSounds.TRAVELLER_CELEBRATE.get();
        } else if (seesPlayerHoldingLovedItem(piglin)) {
            return PPSounds.TRAVELLER_JEALOUS.get();
        } else {
            return isNearRepellent(piglin) ? PPSounds.TRAVELLER_RETREAT.get() : PPSounds.TRAVELLER_IDLE.get();
        }
    }
}
