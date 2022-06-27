package tallestred.piglinproliferation.common.entities.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.RememberIfHoglinWasKilled;
import net.minecraft.world.entity.monster.piglin.StartAdmiringItemIfSeen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import tallestred.piglinproliferation.common.PPLootTables;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;
import tallestred.piglinproliferation.common.entities.ai.behaviors.BowAttack;
import tallestred.piglinproliferation.common.entities.ai.behaviors.StopHoldingItemIfNoLongerAdmiringAlchemist;
import tallestred.piglinproliferation.common.entities.ai.behaviors.ThrowPotionAtSelfTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PiglinAlchemistAi extends PiglinAi {
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
    private static final Method idle = ObfuscationReflectionHelper.findMethod(PiglinAi.class, "m_34891_",
            Brain.class);
    private static final Method core = ObfuscationReflectionHelper.findMethod(PiglinAi.class, "m_34820_",
            Brain.class); // This has to be done because I don't feel like copying and pasting every method from PiglinAi

    public PiglinAlchemistAi() {

    }

    public static Brain<?> makeBrain(PiglinAlchemist piglin, Brain<PiglinAlchemist> brain) {
        try {
            PiglinAlchemistAi.idle.invoke(PiglinAi.class, brain);
            PiglinAlchemistAi.initCoreActivity(brain);
            PiglinAlchemistAi.admireItem.invoke(PiglinAi.class, brain);
            PiglinAlchemistAi.initFightActivity(piglin, brain);
            PiglinAlchemistAi.celebrateActivity.invoke(PiglinAi.class, brain);
            PiglinAlchemistAi.retreatActivity.invoke(PiglinAi.class, brain);
            PiglinAlchemistAi.hoglinRiding.invoke(PiglinAi.class, brain);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            new RuntimeException("Reflection failed, please report to the Piglin-Proliferation github");
        }
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initFightActivity(PiglinAlchemist piglin, Brain<PiglinAlchemist> p_34905_) {
        p_34905_.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.<net.minecraft.world.entity.ai.behavior.Behavior<? super Piglin>>of(new StopAttackingIfTargetInvalid<>((target) -> {
            return !isNearestValidAttackTarget(piglin, target);
        }), new RunIf<>(PiglinAlchemistAi::hasCrossbow, new BackUpIfTooClose(5, 0.75F)), new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F), new MeleeAttack(20), new CrossbowAttack(), new RememberIfHoglinWasKilled(), new BowAttack(1.5F, 15.0F, 20), new EraseMemoryIf<>(PiglinAlchemistAi::isNearZombified, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCoreActivity(Brain<PiglinAlchemist> p_34821_) {
        p_34821_.addActivity(Activity.CORE, 0, ImmutableList.<Behavior<? super PiglinAlchemist>>of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), new InteractWithDoor(), new ThrowPotionAtSelfTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.FIRE_RESISTANCE), (alchemist) -> alchemist.isAlive() && alchemist.isOnFire()), babyAvoidNemesis(), avoidZombified(), new StopHoldingItemIfNoLongerAdmiringAlchemist<>(), new StartAdmiringItemIfSeen<>(120), new StartCelebratingIfTargetDead(300, PiglinAlchemistAi::wantsToDance), new StopBeingAngryIfTargetDead<>()));
    }

    private static CopyMemoryWithExpiry<Piglin, LivingEntity> babyAvoidNemesis() {
        return new CopyMemoryWithExpiry<>(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, BABY_AVOID_NEMESIS_DURATION);
    }

    private static CopyMemoryWithExpiry<Piglin, LivingEntity> avoidZombified() {
        return new CopyMemoryWithExpiry<>(PiglinAlchemistAi::isNearZombified, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, AVOID_ZOMBIFIED_DURATION);
    }

    private static boolean wantsToDance(LivingEntity p_34811_, LivingEntity p_34812_) {
        if (p_34812_.getType() != EntityType.HOGLIN) {
            return false;
        } else {
            return RandomSource.create(p_34811_.level.getGameTime()).nextFloat() < 0.1F;
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
}
