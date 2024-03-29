package tallestred.piglinproliferation.common.entities.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import tallestred.piglinproliferation.PPActivities;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;
import tallestred.piglinproliferation.common.entities.ai.behaviors.MoveAroundPiglins;
import tallestred.piglinproliferation.common.entities.ai.behaviors.StopHoldingItemAfterAdmiring;
import tallestred.piglinproliferation.common.entities.ai.behaviors.SwimOnlyOutOfLava;
import tallestred.piglinproliferation.common.entities.ai.behaviors.TravellerSit;
import tallestred.piglinproliferation.common.loot.PPLoot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PiglinTravellerAi extends PiglinAi {
    private static final UniformInt RIDE_START_INTERVAL = TimeUtil.rangeOfSeconds(10, 40);
    private static final UniformInt RIDE_DURATION = TimeUtil.rangeOfSeconds(10, 30);
    private static final UniformInt AVOID_ZOMBIFIED_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final UniformInt BABY_AVOID_NEMESIS_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final Method hoglinRiding = ObfuscationReflectionHelper.findMethod(PiglinAi.class, "m_34973_", Brain.class);
    private static final Method retreatActivity = ObfuscationReflectionHelper.findMethod(PiglinAi.class, "m_34958_", Brain.class);
    private static final Method celebrateActivity = ObfuscationReflectionHelper.findMethod(PiglinAi.class, "m_34920_", Brain.class);
    private static final Method admireItem = ObfuscationReflectionHelper.findMethod(PiglinAi.class, "m_34940_", Brain.class);
    // This has to be done because I don't feel like copying and pasting every method from PiglinAi

    public PiglinTravellerAi() {

    }

    public static Brain<?> makeBrain(PiglinTraveller piglin, Brain<PiglinTraveller> brain) {
        try {
            initIdleActivity(brain);
            initCoreActivity(brain, piglin);
            admireItem.invoke(PiglinAi.class, brain);
            initFightActivity(piglin, brain);
            celebrateActivity.invoke(PiglinAi.class, brain);
            retreatActivity.invoke(PiglinAi.class, brain);
            hoglinRiding.invoke(PiglinAi.class, brain);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            new RuntimeException("Reflection failed, please report to the Piglin-Proliferation github");
        }
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initFightActivity(PiglinTraveller piglin, Brain<PiglinTraveller> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.<net.minecraft.world.entity.ai.behavior.BehaviorControl<? super Piglin>>of(StopAttackingIfTargetInvalid.create((p_34981_) -> {
            return !isNearestValidAttackTarget(piglin, p_34981_);
        }), BehaviorBuilder.triggerIf(PiglinTravellerAi::hasCrossbow, BackUpIfTooClose.create(5, 0.75F)), SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F), MeleeAttack.create(20), new CrossbowAttack(), RememberIfHoglinWasKilled.create(), EraseMemoryIf.create(PiglinTravellerAi::isNearZombified, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCoreActivity(Brain<PiglinTraveller> brain, PiglinTraveller traveller) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.<net.minecraft.world.entity.ai.behavior.BehaviorControl<? super PiglinTraveller>>of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), InteractWithDoor.create(), new SwimOnlyOutOfLava(0.8F), avoidZombified(), StopHoldingItemAfterAdmiring.create(PPLoot.TRAVELLER_BARTER), StartAdmiringItemIfSeen.create(120), StartCelebratingIfTargetDead.create(300, PiglinTravellerAi::wantsToDanceOnHoglin), StopBeingAngryIfTargetDead.create()));
    }

    private static void initIdleActivity(Brain<PiglinTraveller> pBrain) {
        pBrain.addActivity(Activity.IDLE, 10, ImmutableList.of(SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0F), StartAttacking.<Piglin>create(AbstractPiglin::isAdult, PiglinTravellerAi::findNearestValidAttackTarget), BehaviorBuilder.triggerIf(PiglinTraveller::canHunt, StartHuntingHoglin.create()), avoidRepellent(), babySometimesRideBabyHoglin(), createIdleLookBehaviors(), createIdleMovementBehaviors(), SetLookAndInteract.create(EntityType.PLAYER, 4)));
    }

    private static BehaviorControl<LivingEntity> babySometimesRideBabyHoglin() {
        SetEntityLookTargetSometimes.Ticker setentitylooktargetsometimes$ticker = new SetEntityLookTargetSometimes.Ticker(RIDE_START_INTERVAL);
        return CopyMemoryWithExpiry.create((p_258952_) -> {
            return p_258952_.isBaby() && setentitylooktargetsometimes$ticker.tickDownAndCheck(p_258952_.level().random);
        }, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_DURATION);
    }


    private static RunOne<LivingEntity> createIdleLookBehaviors() {
        return new RunOne<>(ImmutableList.<Pair<? extends BehaviorControl<? super LivingEntity>, Integer>>builder().addAll(createLookBehaviors()).add(Pair.of(new DoNothing(30, 60), 1)).build());
    }

    private static RunOne<Piglin> createIdleMovementBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(MoveAroundPiglins.moveAroundPiglins(0.6F, true), 2), Pair.of(new TravellerSit(), 1), Pair.of(RandomStroll.stroll(0.6F), 2), Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(BehaviorBuilder.triggerIf(PiglinTravellerAi::doesntSeeAnyPlayerHoldingLovedItem, SetWalkTargetFromLookTarget.create(0.6F, 3)), 2), Pair.of(StrollToPoi.create(MemoryModuleType.HOME, 0.6F, 2, 100), 2), Pair.of(StrollAroundPoi.create(MemoryModuleType.HOME, 0.6F, 5), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    private static ImmutableList<Pair<OneShot<LivingEntity>, Integer>> createLookBehaviors() {
        return ImmutableList.of(Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1), Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0F), 1), Pair.of(SetEntityLookTarget.create(8.0F), 1));
    }

    private static boolean doesntSeeAnyPlayerHoldingLovedItem(LivingEntity p_34983_) {
        return !seesPlayerHoldingLovedItem(p_34983_);
    }

    private static boolean seesPlayerHoldingLovedItem(LivingEntity p_34972_) {
        return p_34972_.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static BehaviorControl<PathfinderMob> avoidRepellent() {
        return SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, false);
    }

    private static BehaviorControl<Piglin> babyAvoidNemesis() {
        return CopyMemoryWithExpiry.create(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, BABY_AVOID_NEMESIS_DURATION);
    }

    private static BehaviorControl<Piglin> avoidZombified() {
        return CopyMemoryWithExpiry.create(PiglinTravellerAi::isNearZombified, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, AVOID_ZOMBIFIED_DURATION);
    }

    protected static boolean wantsToDanceOnHoglin(LivingEntity p_34811_, LivingEntity p_34812_) {
        if (p_34812_.getType() != EntityType.HOGLIN) {
            return false;
        } else {
            return RandomSource.create(p_34811_.level().getGameTime()).nextFloat() < 0.1F;
        }
    }

    private static void putInInventory(Piglin piglin, ItemStack item) {
        ItemStack itemstack = piglin.getInventory().addItem(item);
        throwItemsTowardRandomPos(piglin, Collections.singletonList(itemstack));
    }

    private static void throwItems(Piglin p_34861_, List<ItemStack> p_34862_) {
        Optional<Player> optional = p_34861_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (optional.isPresent()) {
            throwItemsTowardPlayer(p_34861_, optional.get(), p_34862_);
        } else {
            throwItemsTowardRandomPos(p_34861_, p_34862_);
        }

    }

    private static void throwItemsTowardRandomPos(Piglin p_34913_, List<ItemStack> p_34914_) {
        throwItemsTowardPos(p_34913_, p_34914_, getRandomNearbyPos(p_34913_));
    }

    private static void throwItemsTowardPlayer(Piglin p_34851_, Player p_34852_, List<ItemStack> p_34853_) {
        throwItemsTowardPos(p_34851_, p_34853_, p_34852_.position());
    }

    private static void throwItemsTowardPos(Piglin p_34864_, List<ItemStack> p_34865_, Vec3 p_34866_) {
        if (!p_34865_.isEmpty()) {
            p_34864_.swing(InteractionHand.OFF_HAND);

            for (ItemStack itemstack : p_34865_) {
                BehaviorUtils.throwItem(p_34864_, itemstack, p_34866_.add(0.0D, 1.0D, 0.0D));
            }
        }

    }

    private static List<ItemStack> getBarterResponseItems(Piglin piglin) {
        LootTable loottable = piglin.level().getServer().getLootData().getLootTable(PPLoot.TRAVELLER_BARTER);
        List<ItemStack> list = loottable.getRandomItems((new LootParams.Builder((ServerLevel) piglin.level())).withParameter(LootContextParams.ORIGIN, piglin.position()).withParameter(LootContextParams.THIS_ENTITY, piglin).create(LootContextParamSets.PIGLIN_BARTER));
        return list;
    }

    private static boolean hasCrossbow(LivingEntity entity) {
        return entity.isHolding((is) -> {
            return is.getItem() instanceof CrossbowItem;
        });
    }

    private static boolean isNearestValidAttackTarget(Piglin pPiglin, LivingEntity pTarget) {
        return findNearestValidAttackTarget(pPiglin).filter((p_34887_) -> {
            return p_34887_ == pTarget;
        }).isPresent();
    }

    private static boolean isNearZombified(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
            LivingEntity livingentity = (LivingEntity) brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get();
            return piglin.closerThan(livingentity, 6.0);
        } else {
            return false;
        }
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        if (isNearZombified(piglin)) {
            return Optional.empty();
        } else {
            Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(piglin, MemoryModuleType.ANGRY_AT);
            if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(piglin, (LivingEntity) optional.get())) {
                return optional;
            } else {
                Optional optional3;
                if (brain.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
                    optional3 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
                    if (optional3.isPresent()) {
                        return optional3;
                    }
                }

                optional3 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
                if (optional3.isPresent()) {
                    return optional3;
                } else if (!piglin.level().getBiome(piglin.getOnPos()).is(Biomes.WARPED_FOREST)){
                    Optional<Player> optional2 = brain.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
                    return optional2.isPresent() && Sensor.isEntityAttackable(piglin, (LivingEntity) optional2.get()) ? optional2 : Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    private static Vec3 getRandomNearbyPos(Piglin piglin) {
        Vec3 vec3 = LandRandomPos.getPos(piglin, 4, 2);
        return vec3 == null ? piglin.position() : vec3;
    }

    private static boolean isBabyRidingBaby(Piglin piglin) {
        if (!piglin.isBaby()) {
            return false;
        } else {
            Entity entity = piglin.getVehicle();
            return entity instanceof Piglin && ((Piglin) entity).isBaby() || entity instanceof Hoglin && ((Hoglin) entity).isBaby();
        }
    }

    public static Optional<SoundEvent> getAlchemistSoundForCurrentActivity(PiglinTraveller piglin) {
        return piglin.getBrain().getActiveNonCoreActivity().map((activity) -> getAlchemistSoundForActivity(piglin, activity));
    }

    private static SoundEvent getAlchemistSoundForActivity(Piglin piglin, Activity activity) {
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

    private static boolean isNearAvoidTarget(Piglin p_35003_) {
        Brain<Piglin> brain = p_35003_.getBrain();
        return !brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? false : brain.getMemory(MemoryModuleType.AVOID_TARGET).get().closerThan(p_35003_, 12.0D);
    }

    private static boolean isNearRepellent(Piglin p_35023_) {
        return p_35023_.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }


    public static void updateActivity(PiglinTraveller piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(PPActivities.THROW_POTION_ACTIVITY.get(), Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE));
        Activity activity1 = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != activity1) getAlchemistSoundForCurrentActivity(piglin).ifPresent(piglin::playSoundEvent);
        piglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        if (!brain.hasMemoryValue(MemoryModuleType.RIDE_TARGET) && isBabyRidingBaby(piglin)) piglin.stopRiding();
        if (!brain.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) brain.eraseMemory(MemoryModuleType.DANCING);
        piglin.setDancing(brain.hasMemoryValue(MemoryModuleType.DANCING));
    }
}
