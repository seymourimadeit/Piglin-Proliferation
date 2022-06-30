package tallestred.piglinproliferation.common.entities.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import tallestred.piglinproliferation.PPActivities;
import tallestred.piglinproliferation.PPMemoryModules;
import tallestred.piglinproliferation.common.PPLootTables;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;
import tallestred.piglinproliferation.common.entities.ai.behaviors.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class PiglinAlchemistAi extends PiglinAi {
    private static final UniformInt RIDE_START_INTERVAL = TimeUtil.rangeOfSeconds(10, 40);
    private static final UniformInt RIDE_DURATION = TimeUtil.rangeOfSeconds(10, 30);
    private static final UniformInt AVOID_ZOMBIFIED_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final UniformInt BABY_AVOID_NEMESIS_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final Method hoglinRiding = ObfuscationReflectionHelper.findMethod(PiglinAi.class, "m_34973_",
            Brain.class);
    private static final Method retreatActivity = ObfuscationReflectionHelper.findMethod(PiglinAi.class, "m_34958_",
            Brain.class);
    private static final Method celebrateActivity = ObfuscationReflectionHelper.findMethod(PiglinAi.class, "m_34920_",
            Brain.class);
    private static final Method admireItem = ObfuscationReflectionHelper.findMethod(PiglinAi.class, "m_34940_",
            Brain.class);
    // This has to be done because I don't feel like copying and pasting every method from PiglinAi

    public PiglinAlchemistAi() {

    }

    public static Brain<?> makeBrain(PiglinAlchemist piglin, Brain<PiglinAlchemist> brain) {
        try {
            PiglinAlchemistAi.initIdleActivity(brain);
            PiglinAlchemistAi.initCoreActivity(brain, piglin);
            PiglinAlchemistAi.admireItem.invoke(PiglinAi.class, brain);
            PiglinAlchemistAi.initFightActivity(piglin, brain);
            PiglinAlchemistAi.celebrateActivity.invoke(PiglinAi.class, brain);
            PiglinAlchemistAi.retreatActivity.invoke(PiglinAi.class, brain);
            PiglinAlchemistAi.hoglinRiding.invoke(PiglinAi.class, brain);
            PiglinAlchemistAi.initThrowPotionActivity(brain, piglin);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            new RuntimeException("Reflection failed, please report to the Piglin-Proliferation github");
        }
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initFightActivity(PiglinAlchemist piglin, Brain<PiglinAlchemist> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.<net.minecraft.world.entity.ai.behavior.Behavior<? super PiglinAlchemist>>of(new StopAttackingIfTargetInvalid<>((target) -> {
            return !isNearestValidAttackTarget(piglin, target);
        }), new RunIf<>(tallestred.piglinproliferation.common.entities.ai.PiglinAlchemistAi::hasCrossbow, new BackUpIfTooClose(5, 0.75F)), new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F), new MeleeAttack(20), new CrossbowAttack(), new RememberIfHoglinWasKilled(), new BowAttack(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 1.5F, 15.0F, 20), new EraseMemoryIf<>(tallestred.piglinproliferation.common.entities.ai.PiglinAlchemistAi::isNearZombified, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCoreActivity(Brain<PiglinAlchemist> brain, PiglinAlchemist alchmeist) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.<Behavior<? super PiglinAlchemist>>of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), new InteractWithDoor(), new SwimOnlyOutOfLava(0.8F), avoidZombified(), generatePotionAi(alchmeist), new StopHoldingItemIfNoLongerAdmiringAlchemist<>(), new ShootTippedArrow(1.5F, 15.0F, 20, PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), Potions.STRONG_HEALING), (piglin -> piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth())), new StartAdmiringItemIfSeen<>(120), new StartCelebratingIfTargetDead(300, PiglinAlchemistAi::wantsToDance), new StopBeingAngryIfTargetDead<>()));
    }

    private static void initIdleActivity(Brain<PiglinAlchemist> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.<net.minecraft.world.entity.ai.behavior.Behavior<? super PiglinAlchemist>>of(new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 14.0F), new StartAttacking<>(AbstractPiglin::isAdult, PiglinAlchemistAi::findNearestValidAttackTarget), new RunIf<>(PiglinAlchemist::canHunt, new StartHuntingHoglin<>()), avoidRepellent(), babySometimesRideBabyHoglin(), createIdleLookBehaviors(), createIdleMovementBehaviors(), new SetLookAndInteract(EntityType.PLAYER, 4)));
    }

    private static void initThrowPotionActivity(Brain<PiglinAlchemist> brain, PiglinAlchemist piglin) {
        brain.addActivityAndRemoveMemoryWhenStopped(PPActivities.THROW_POTION_ACTIVITY.get(), 10, ImmutableList.<Behavior<? super PiglinAlchemist>>of(new ShootTippedArrow(1.5F, 15.0F, 20, PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), Potions.STRONG_HEALING), (piglin2 -> piglin2.isAlive() && piglin2.getHealth() < piglin2.getMaxHealth())), generatePotionAi(piglin)), PPMemoryModules.POTION_THROW_TARGET.get());
    }

    private static RunSometimes<Piglin> babySometimesRideBabyHoglin() {
        return new RunSometimes<>(new CopyMemoryWithExpiry<>(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_DURATION), RIDE_START_INTERVAL);
    }

    private static RunOne<Piglin> createIdleLookBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 1), Pair.of(new SetEntityLookTarget(EntityType.PIGLIN, 8.0F), 1), Pair.of(new SetEntityLookTarget(8.0F), 1), Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<Piglin> createIdleMovementBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(new MoveAroundPiglins(0.8F), 2), Pair.of(new RandomStroll(0.6F), 2), Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(new RunIf<>(tallestred.piglinproliferation.common.entities.ai.PiglinAlchemistAi::doesntSeeAnyPlayerHoldingLovedItem, new SetWalkTargetFromLookTarget(0.6F, 3)), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<PiglinAlchemist> generatePotionAi(PiglinAlchemist piglinAlchemist) {
        return new RunOne<>(ImmutableList.of(
                Pair.of(new ThrowPotionAtTargetTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.FIRE_RESISTANCE),
                        (alchemist) -> alchemist.isAlive(), (piglin) -> piglin.isAlive() && piglin.isOnFire()), 1),
                Pair.of(new ThrowPotionAtTargetTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_REGENERATION),
                        (alchemist) -> alchemist.isAlive(), (piglin) -> {
                    List<AbstractPiglin> list = piglinAlchemist.level.getEntitiesOfClass(AbstractPiglin.class, piglinAlchemist.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
                    if (!list.isEmpty()) {
                        for (AbstractPiglin piglin1 : list) {
                            if (piglin1.getTarget() != null || piglinAlchemist.getTarget() != null)
                                return list.size() > 2 && piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth();
                            // This makes it so alchemists don't
                            // attempt to throw a healing potion if theres 2 or less of them, as if they did it would make it so only one is attacking while the other is failing to throw the potion because the
                            // attacker would just keep pushing into them
                        }
                    }
                    return piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth();
                }), 1),
                Pair.of(new ThrowPotionAtTargetTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_HEALING), (alchemist) -> {
                    return alchemist.isAlive();
                }, (piglin) -> {
                    List<AbstractPiglin> list = piglinAlchemist.level.getEntitiesOfClass(AbstractPiglin.class, piglinAlchemist.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
                    if (!list.isEmpty()) {
                        for (AbstractPiglin piglin1 : list) {
                            if (piglin1.getTarget() != null || piglinAlchemist.getTarget() != null)
                                return piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth() && !piglinAlchemist.beltInventory.stream().anyMatch(itemStack -> PotionUtils.getPotion(itemStack) == Potions.STRONG_REGENERATION) && list.size() > 2; // This makes it so alchemists don't
                            // attempt to throw a healing potion if theres only 2 or less of them, as if they did it would make it so only one is attacking while the other is failing to throw the potion because the
                            // attacker would just keep pushing into them
                        }
                    }
                    return piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth();
                }), 1),
                Pair.of(new ThrowPotionAtTargetTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_STRENGTH), (alchemist) -> alchemist.isAlive(), (piglin) -> piglin.isAlive() && piglin.getTarget() != null && piglin.getHealth() < (piglin.getMaxHealth() / 2) && !piglin.isHolding((itemStack) -> {
                    Item itemInStack = itemStack.getItem();
                    return itemInStack instanceof ProjectileWeaponItem;
                })) {
                    @Override
                    protected void start(ServerLevel level, PiglinAlchemist alchemist, long gameTime) {
                        super.start(level, alchemist, gameTime);
                        Mob piglinsCalled = alchemist.getBrain().getMemory(PPMemoryModules.POTION_THROW_TARGET.get()).orElseGet(null);
                        piglinsCalled.getNavigation().moveTo(alchemist, 1.0D);
                    }

                    @Override
                    protected void tick(ServerLevel level, PiglinAlchemist alchemist, long gameTime) {
                        super.tick(level, alchemist, gameTime);
                        Mob piglinsCalled = alchemist.getBrain().getMemory(PPMemoryModules.POTION_THROW_TARGET.get()).orElseGet(null);
                        piglinsCalled.getNavigation().moveTo(alchemist, 1.0D);
                    }
                }, 1),
                Pair.of(new ThrowPotionAtTargetTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.FIRE_RESISTANCE),
                        (alchemist) -> alchemist.isAlive(), (piglin) -> piglin.isAlive() && piglin.isOnFire()), 1),
                Pair.of(new ThrowPotionAtSelfTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_REGENERATION),
                        (alchemist) -> alchemist.isAlive() && alchemist.getHealth() < alchemist.getMaxHealth()), 1),
                Pair.of(new ThrowPotionAtSelfTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.FIRE_RESISTANCE),
                        (alchemist) -> alchemist.isAlive() && alchemist.isOnFire()), 1),
                Pair.of(new ThrowPotionAtSelfTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_HEALING),
                        (alchemist) -> alchemist.isAlive() && alchemist.getHealth() < alchemist.getMaxHealth() && !alchemist.beltInventory.stream().anyMatch(itemStack -> PotionUtils.getPotion(itemStack) == Potions.STRONG_REGENERATION)), 2)));
    }


    private static boolean doesntSeeAnyPlayerHoldingLovedItem(LivingEntity p_34983_) {
        return !seesPlayerHoldingLovedItem(p_34983_);
    }

    private static boolean seesPlayerHoldingLovedItem(LivingEntity p_34972_) {
        return p_34972_.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static CopyMemoryWithExpiry<Piglin, LivingEntity> babyAvoidNemesis() {
        return new CopyMemoryWithExpiry<>(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, BABY_AVOID_NEMESIS_DURATION);
    }

    private static CopyMemoryWithExpiry<Piglin, LivingEntity> avoidZombified() {
        return new CopyMemoryWithExpiry<>(tallestred.piglinproliferation.common.entities.ai.PiglinAlchemistAi::isNearZombified, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, AVOID_ZOMBIFIED_DURATION);
    }

    private static SetWalkTargetAwayFrom<BlockPos> avoidRepellent() {
        return SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, false);
    }


    private static boolean wantsToDance(LivingEntity p_34811_, LivingEntity p_34812_) {
        if (p_34812_.getType() != EntityType.HOGLIN) {
            return false;
        } else {
            return (new Random(p_34811_.level.getGameTime())).nextFloat() < 0.1F;
        }
    }

    // Now I feel like it
    public static void stopHoldingOffHandItem(Piglin piglin, boolean barter) {
        ItemStack itemstack = piglin.getItemInHand(InteractionHand.OFF_HAND);
        piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        if (piglin.isAdult()) {
            boolean flag = itemstack.isPiglinCurrency();
            if (barter && flag) {
                throwItems(piglin, getBarterResponseItems(piglin));
            } else if (!flag) {
                boolean flag1 = piglin.equipItemIfPossible(itemstack);
                if (!flag1) {
                    putInInventory(piglin, itemstack);
                }
            }
        } else {
            boolean flag2 = piglin.equipItemIfPossible(itemstack);
            if (!flag2) {
                ItemStack itemstack1 = piglin.getMainHandItem();
                if (isLovedItem(itemstack1)) {
                    putInInventory(piglin, itemstack1);
                } else {
                    throwItems(piglin, Collections.singletonList(itemstack1));
                }

                piglin.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
                piglin.setGuaranteedDrop(EquipmentSlot.MAINHAND);
                piglin.setPersistenceRequired();
            }
        }

    }

    private static void putInInventory(Piglin piglin, ItemStack item) {
        ItemStack itemstack = ((SimpleContainer) piglin.getInventory()).addItem(item);
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

    private static List<ItemStack> getBarterResponseItems(Piglin p_34997_) {
        LootTable loottable = p_34997_.level.getServer().getLootTables().get(PPLootTables.ALCHEMIST_BARTER);
        List<ItemStack> list = loottable.getRandomItems((new LootContext.Builder((ServerLevel) p_34997_.level)).withParameter(LootContextParams.THIS_ENTITY, p_34997_).withRandom(p_34997_.level.random).create(LootContextParamSets.PIGLIN_BARTER));
        return list;
    }

    private static boolean hasCrossbow(LivingEntity entity) {
        return entity.isHolding((is) -> {
            return is.getItem() instanceof CrossbowItem;
        });
    }

    private static boolean isNearestValidAttackTarget(Piglin p_34901_, LivingEntity p_34902_) {
        return findNearestValidAttackTarget(p_34901_).filter((p_34887_) -> {
            return p_34887_ == p_34902_;
        }).isPresent();
    }

    private static boolean isNearZombified(Piglin p_34999_) {
        Brain<Piglin> brain = p_34999_.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
            LivingEntity livingentity = (LivingEntity) brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get();
            return p_34999_.closerThan(livingentity, 6.0);
        } else {
            return false;
        }
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Piglin p_35001_) {
        Brain<Piglin> brain = p_35001_.getBrain();
        if (isNearZombified(p_35001_)) {
            return Optional.empty();
        } else {
            Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(p_35001_, MemoryModuleType.ANGRY_AT);
            if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(p_35001_, (LivingEntity) optional.get())) {
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
                } else {
                    Optional<Player> optional2 = brain.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
                    return optional2.isPresent() && Sensor.isEntityAttackable(p_35001_, (LivingEntity) optional2.get()) ? optional2 : Optional.empty();
                }
            }
        }
    }

    private static Vec3 getRandomNearbyPos(Piglin p_35017_) {
        Vec3 vec3 = LandRandomPos.getPos(p_35017_, 4, 2);
        return vec3 == null ? p_35017_.position() : vec3;
    }

    private static boolean isBabyRidingBaby(Piglin piglin) {
        if (!piglin.isBaby()) {
            return false;
        } else {
            Entity entity = piglin.getVehicle();
            return entity instanceof Piglin && ((Piglin) entity).isBaby() || entity instanceof Hoglin && ((Hoglin) entity).isBaby();
        }
    }

    public static void updateActivity(PiglinAlchemist piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(PPActivities.THROW_POTION_ACTIVITY.get(), Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE));
        Activity activity1 = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != activity1)
            getSoundForCurrentActivity(piglin).ifPresent(piglin::playSound);
        piglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        if (!brain.hasMemoryValue(MemoryModuleType.RIDE_TARGET) && isBabyRidingBaby(piglin))
            piglin.stopRiding();
        if (!brain.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION))
            brain.eraseMemory(MemoryModuleType.DANCING);
        piglin.setDancing(brain.hasMemoryValue(MemoryModuleType.DANCING));
    }
}
