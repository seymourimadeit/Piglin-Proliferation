package tallestred.piglinproliferation.common.entities.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.PPActivities;
import tallestred.piglinproliferation.common.entities.ai.behaviors.SafeCrossbowAttack;
import tallestred.piglinproliferation.common.entities.ai.behaviors.SwimOnlyOutOfLava;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static tallestred.piglinproliferation.util.CodeUtilities.castElementsToList;
import static tallestred.piglinproliferation.util.CodeUtilities.castOrThrow;

public abstract class AbstractPiglinAi<P extends Piglin> extends PiglinAi {
    public Brain<?> populateBrain(P piglin, Brain<P> brain) {
        initActivities(piglin, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    //Warnings suppressed because the class cast error is properly handled
    @SuppressWarnings("unchecked")
    public void initActivities(P piglin, Brain<P> brain) {
        setupIdleActivity(brain, piglin);
        setupCoreActivity(brain, piglin);
        try {
            Brain<Piglin> piglinBrain = (Brain<Piglin>) brain;
            PiglinAi.initAdmireItemActivity(piglinBrain);
            setupFightActivity(brain, piglin);
            PiglinAi.initCelebrateActivity(piglinBrain);
            PiglinAi.initRetreatActivity(piglinBrain);
            setupRideHoglinActivity(piglinBrain);
        } catch (ClassCastException e) {
            System.out.println("Something went wrong with PiglinAi casts - report to the Piglin Proliferation github.");
            throw e;
        }
    }

    public void setupRideHoglinActivity(Brain<Piglin> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.RIDE, 10, ImmutableList.of(Mount.create(0.8F), SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 8.0F), BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(Entity::isPassenger), TriggerGate.triggerOneShuffled(castOrThrow(ImmutableList.builder().addAll(lookBehaviors()).add(Pair.of(BehaviorBuilder.triggerIf((p_258950_) -> {
            return true;
        }), 1)).build()))), DismountOrSkipMounting.create(8, PiglinAi::wantsToStopRiding)), MemoryModuleType.RIDE_TARGET);
    }

    private void setupCoreActivity(Brain<P> brain, P piglin) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.copyOf(coreBehaviors(piglin)));
    }

    private void setupIdleActivity(Brain<P> pBrain, P piglin) {
        pBrain.addActivity(Activity.IDLE, 10, ImmutableList.copyOf(idleBehaviors(piglin)));
    }

    private void setupFightActivity(Brain<P> brain, P piglin) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.copyOf(fightBehaviors(piglin)), MemoryModuleType.ATTACK_TARGET);
    }

    protected List<BehaviorControl<? super P>> coreBehaviors(P piglin) {
        return castElementsToList(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                InteractWithDoor.create(),
                new SwimOnlyOutOfLava(0.8F),
                PiglinAi.avoidZombified(),
                StopHoldingItemIfNoLongerAdmiring.create(),
                StartAdmiringItemIfSeen.create(120),
                StartCelebratingIfTargetDead.create(300, PiglinAi::wantsToDance),
                StopBeingAngryIfTargetDead.create()
        );
    }

    protected List<BehaviorControl<? super P>> idleBehaviors(P piglin) {
        return castElementsToList(
                SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
                StartAttacking.create(AbstractPiglin::isAdult, this::nearestValidAttackTarget),
                BehaviorBuilder.triggerIf(Piglin::canHunt, StartHuntingHoglin.create()),
                PiglinAi.avoidRepellent(),
                PiglinAi.babySometimesRideBabyHoglin(),
                idleLookBehaviors(),
                idleMovementBehaviors(),
                SetLookAndInteract.create(EntityType.PLAYER, 4)
        );
    }

    protected ImmutableList<Pair<OneShot<LivingEntity>, Integer>> lookBehaviors() {
        return PiglinAi.createLookBehaviors();
    }

    protected RunOne<LivingEntity> idleLookBehaviors() {
        return PiglinAi.createIdleLookBehaviors();
    }

    protected RunOne<P> idleMovementBehaviors() {
        return castOrThrow(PiglinAi.createIdleMovementBehaviors());
    }

    protected List<BehaviorControl<? super P>> fightBehaviors(P piglin) {
        return castElementsToList(
                StopAttackingIfTargetInvalid.create((entity) -> {
                    return !isEntityValidForAttack(piglin, entity);
                }),
                BehaviorBuilder.triggerIf(PiglinAi::hasCrossbow, BackUpIfTooClose.create(5, 0.75F)),
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                MeleeAttack.create(20),
                new SafeCrossbowAttack<>(),
                RememberIfHoglinWasKilled.create(),
                EraseMemoryIf.create(PiglinAi::isNearZombified, MemoryModuleType.ATTACK_TARGET)
        );
    }

    private boolean isEntityValidForAttack(P piglin, LivingEntity target) {
        return nearestValidAttackTarget(piglin).filter((p_34887_) -> {
            return p_34887_ == target;
        }).isPresent();
    }

    protected Optional<? extends LivingEntity> nearestValidAttackTarget(Piglin piglin) {
        return PiglinAi.findNearestValidAttackTarget(piglin);
    }

    public static Optional<? extends LivingEntity> findNemesisTarget(Brain<? extends Piglin> brain) {
        Optional<? extends LivingEntity> optional;
        if (brain.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
            optional = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
            if (optional.isPresent()) {
                return optional;
            }
        }
        optional = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
        return optional;
    }

    public void stopHoldingOffHandItem(P piglin, boolean barter, ResourceKey<LootTable> lootTableLocation) {
        ItemStack stack = piglin.getItemInHand(InteractionHand.OFF_HAND);
        piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        if (piglin.isAdult()) {
            boolean stackIsPiglinCurrency = stack.isPiglinCurrency();
            if (barter && stackIsPiglinCurrency) {
                MinecraftServer server = piglin.level().getServer();
                if (server != null)
                    throwItems(piglin, server.reloadableRegistries().getLootTable(lootTableLocation).getRandomItems((new LootParams.Builder((ServerLevel) piglin.level())).withParameter(LootContextParams.ORIGIN, piglin.position()).withParameter(LootContextParams.THIS_ENTITY, piglin).create(LootContextParamSets.PIGLIN_BARTER)));
            } else if (!stackIsPiglinCurrency) {
                boolean flag1 = piglin.equipItemIfPossible(stack).isEmpty();
                if (!flag1) {
                    putInInventory(piglin, stack);
                }
            }
        } else {
            boolean flag2 = piglin.equipItemIfPossible(stack).isEmpty();
            if (!flag2) {
                ItemStack itemstack1 = piglin.getMainHandItem();
                if (isLovedItem(itemstack1)) {
                    putInInventory(piglin, itemstack1);
                } else {
                    throwItems(piglin, Collections.singletonList(itemstack1));
                }

                piglin.setItemSlot(EquipmentSlot.MAINHAND, stack);
                piglin.setGuaranteedDrop(EquipmentSlot.MAINHAND);
                piglin.setPersistenceRequired();
            }
        }
    }

    protected void putInInventory(P piglin, ItemStack toAdd) {
        ItemStack added = piglin.getInventory().addItem(toAdd);
        throwItemsTowardRandomPos(piglin, Collections.singletonList(added));
    }

    public void throwItems(P piglin, List<ItemStack> stacks) {
        Optional<Player> optional = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (optional.isPresent()) {
            throwItemsTowardPlayer(piglin, optional.get(), stacks);
        } else {
            throwItemsTowardRandomPos(piglin, stacks);
        }

    }

    public void throwItemsTowardRandomPos(P piglin, List<ItemStack> stacks) {
        throwItemsTowardPos(piglin, stacks, getRandomNearbyPos(piglin));
    }

    protected void throwItemsTowardPlayer(P piglin, Player player, List<ItemStack> stacks) {
        throwItemsTowardPos(piglin, stacks, player.position());
    }

    protected void throwItemsTowardPos(P piglin, List<ItemStack> stacks, Vec3 pos) {
        if (!stacks.isEmpty()) {
            piglin.swing(InteractionHand.OFF_HAND);
            for (ItemStack stack : stacks) {
                specificThrowItemBehaviour(piglin, stack);
                BehaviorUtils.throwItem(piglin, stack, pos.add(0.0D, 1.0D, 0.0D));
            }
        }
    }

    protected void specificThrowItemBehaviour(P piglin, ItemStack stack) {}

    public void updateBrainActivity(P piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(PPActivities.THROW_POTION_ACTIVITY.get(), Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE));
        Activity activity1 = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != activity1) soundForCurrentActivity(piglin).ifPresent(piglin::playSound);
        piglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        if (!brain.hasMemoryValue(MemoryModuleType.RIDE_TARGET) && isBabyRidingBaby(piglin)) piglin.stopRiding();
        if (!brain.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) brain.eraseMemory(MemoryModuleType.DANCING);
        piglin.setDancing(brain.hasMemoryValue(MemoryModuleType.DANCING));
    }

    public Optional<SoundEvent> soundForCurrentActivity(P piglin) {
        return piglin.getBrain().getActiveNonCoreActivity().map((activity) -> soundForActivity(piglin, activity));
    }
    public abstract SoundEvent soundForActivity(P piglin, Activity activity);
}