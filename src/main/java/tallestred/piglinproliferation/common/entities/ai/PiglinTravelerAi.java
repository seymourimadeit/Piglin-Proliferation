package tallestred.piglinproliferation.common.entities.ai;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
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
import tallestred.piglinproliferation.common.entities.PiglinTraveler;
import tallestred.piglinproliferation.common.entities.ai.behaviors.*;
import tallestred.piglinproliferation.common.items.TravelersCompassItem;
import tallestred.piglinproliferation.common.loot.PPLoot;

import java.util.List;
import java.util.Optional;

import static tallestred.piglinproliferation.util.CodeUtilities.castElementsToList;

public class PiglinTravelerAi extends AbstractPiglinAi<PiglinTraveler> {
    public static PiglinTravelerAi INSTANCE = new PiglinTravelerAi();

    public PiglinTravelerAi() {
    }

    @Override
    protected List<BehaviorControl<? super PiglinTraveler>> coreBehaviors(PiglinTraveler piglin) {
        return castElementsToList(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                InteractWithDoor.create(),
                new SwimOnlyOutOfLava(0.8F),
                StopHoldingItemAfterAdmiring.create(this, PPLoot.TRAVELER_BARTER/*, PPLoot.TRAVELER_BARTER_CHEAP, PPLoot.TRAVELER_BARTER_EXPENSIVE*/),
                StartAdmiringItemIfSeen.create(120),
                StartCelebratingIfTargetDead.create(300, PiglinAi::wantsToDance),
                StopBeingAngryIfTargetDead.create()
        );
    }

    @Override
    protected List<BehaviorControl<? super PiglinTraveler>> idleBehaviors(PiglinTraveler piglin) {
        return castElementsToList(
                SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
                StartAttacking.create(AbstractPiglin::isAdult, this::nearestValidAttackTarget),
                BehaviorBuilder.triggerIf(Piglin::canHunt, StartHuntingHoglin.create()),
                PiglinAi.babySometimesRideBabyHoglin(),
                idleLookBehaviors(),
                idleMovementBehaviors(),
                SetLookAndInteract.create(EntityType.PLAYER, 4)
        );
    }

    //Not sure if this is the most dynamic but don't want to touch it now
    @Override
    protected RunOne<PiglinTraveler> idleMovementBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(MoveAroundPiglins.moveAroundPiglins(0.6F, true), 2), Pair.of(new TravelerSit<>(), 1), Pair.of(RandomStroll.stroll(0.6F), 2), Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(BehaviorBuilder.triggerIf(PiglinAi::doesntSeeAnyPlayerHoldingLovedItem, SetWalkTargetFromLookTarget.create(0.6F, 3)), 2), Pair.of(StrollToPoi.create(MemoryModuleType.HOME, 0.6F, 2, 100), 2), Pair.of(StrollAroundPoi.create(MemoryModuleType.HOME, 0.6F, 5), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    @Override
    protected List<BehaviorControl<? super PiglinTraveler>> fightBehaviors(PiglinTraveler piglin) {
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
    public void specificThrowItemBehaviour(PiglinTraveler piglin, ItemStack stack) {
        if (stack.getItem() instanceof TravelersCompassItem) {
            piglin.playBarteringAnimation();
            piglin.level().playSound(null, piglin.getX(), piglin.getY(), piglin.getZ(), PPSounds.MAKE_COMPASS.get(), piglin.getSoundSource(), 1.0F, 1.0F);
        }
    }

    @Override
    public SoundEvent soundForActivity(PiglinTraveler piglin, Activity activity) {
        if (activity == Activity.FIGHT) {
            return PPSounds.TRAVELER_ANGRY.get();
        } else if (piglin.isConverting()) {
            return PPSounds.TRAVELER_RETREAT.get();
        } else if (activity == Activity.AVOID && isNearAvoidTarget(piglin)) {
            return PPSounds.TRAVELER_RETREAT.get();
        } else if (activity == Activity.ADMIRE_ITEM) {
            return PPSounds.TRAVELER_ADMIRE.get();
        } else if (activity == Activity.CELEBRATE) {
            return PPSounds.TRAVELER_CELEBRATE.get();
        } else if (seesPlayerHoldingLovedItem(piglin)) {
            return PPSounds.TRAVELER_JEALOUS.get();
        } else {
            return isNearRepellent(piglin) ? PPSounds.TRAVELER_RETREAT.get() : PPSounds.TRAVELER_IDLE.get();
        }
    }
}
