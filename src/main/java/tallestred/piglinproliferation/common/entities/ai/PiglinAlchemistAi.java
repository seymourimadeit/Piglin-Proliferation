package tallestred.piglinproliferation.common.entities.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.piglin.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import tallestred.piglinproliferation.PPActivities;
import tallestred.piglinproliferation.PPMemoryModules;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;
import tallestred.piglinproliferation.common.entities.ai.behaviors.*;
import tallestred.piglinproliferation.common.loot.PPLoot;

import java.util.List;
import java.util.Optional;

import static tallestred.piglinproliferation.util.CodeUtilities.castElementsToList;

public class PiglinAlchemistAi extends AbstractPiglinAi<PiglinAlchemist> {
    public static PiglinAlchemistAi INSTANCE = new PiglinAlchemistAi();

    public PiglinAlchemistAi() {
    }

    @Override
    public void initActivities(PiglinAlchemist piglin, Brain<PiglinAlchemist> brain) {
        super.initActivities(piglin, brain);
        initThrowPotionActivity(brain, piglin);
    }

    @Override
    protected List<BehaviorControl<? super PiglinAlchemist>> coreBehaviors(PiglinAlchemist piglin) {
        return castElementsToList(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                InteractWithDoor.create(),
                new SwimOnlyOutOfLava(0.8F),
                PiglinAi.avoidZombified(),
                StopHoldingItemAfterAdmiring.create(this, PPLoot.ALCHEMIST_BARTER/*, PPLoot.ALCHEMIST_BARTER_CHEAP, PPLoot.ALCHEMIST_BARTER_EXPENSIVE*/),
                StartAdmiringItemIfSeen.create(120),
                StartCelebratingIfTargetDead.create(300, PiglinAi::wantsToDance),
                StopBeingAngryIfTargetDead.create(),
                generatePotionAi(piglin),
                new ShootTippedArrow(1.5F, 15.0F, 20, PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), Potions.STRONG_HEALING), (p -> p.isAlive() && p.getHealth() < p.getMaxHealth()))
        );
    }

    @Override
    protected List<BehaviorControl<? super PiglinAlchemist>> fightBehaviors(PiglinAlchemist piglin) {
        var list = super.fightBehaviors(piglin);
        list.add(new BowAttack<>(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 1.5F, 15.0F, 20));
        return list;
    }

    private static void initThrowPotionActivity(Brain<PiglinAlchemist> brain, PiglinAlchemist piglin) {
        brain.addActivityAndRemoveMemoryWhenStopped(PPActivities.THROW_POTION_ACTIVITY.get(), 10, ImmutableList.<net.minecraft.world.entity.ai.behavior.BehaviorControl<? super PiglinAlchemist>>of(new ShootTippedArrow(1.5F, 15.0F, 20, PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), Potions.STRONG_HEALING), (piglin2 -> piglin2.isAlive() && piglin2.getHealth() < piglin2.getMaxHealth())), generatePotionAi(piglin)), PPMemoryModules.POTION_THROW_TARGET.get());
    }

    private static RunOne<PiglinAlchemist> generatePotionAi(PiglinAlchemist piglinAlchemist) {
        return new RunOne<>(ImmutableList.of(
                Pair.of(new ThrowPotionAtTargetTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.FIRE_RESISTANCE),
                        LivingEntity::isAlive, (piglin) -> piglin.isAlive() && piglin.isOnFire()), 1),
                Pair.of(new ThrowPotionAtTargetTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_REGENERATION),
                        LivingEntity::isAlive, (piglin) -> {
                    List<AbstractPiglin> list = piglinAlchemist.level().getEntitiesOfClass(AbstractPiglin.class, piglinAlchemist.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
                    if (!list.isEmpty()) {
                        for (AbstractPiglin piglin1 : list) {
                            if (piglin1.getTarget() != null || piglinAlchemist.getTarget() != null)
                                return list.size() > 2 && piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth();
                            // This makes it so alchemists don't
                            // attempt to throw a healing potion if there's 2 or less of them, as if they did it would make it so only one is attacking while the other is failing to throw the potion because the
                            // attacker would just keep pushing into them
                        }
                    }
                    return piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth();
                }), 1),
                Pair.of(new ThrowPotionAtTargetTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_HEALING), LivingEntity::isAlive, (piglin) -> {
                    List<AbstractPiglin> list = piglinAlchemist.level().getEntitiesOfClass(AbstractPiglin.class, piglinAlchemist.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
                    if (!list.isEmpty()) {
                        for (AbstractPiglin piglin1 : list) {
                            if (piglin1.getTarget() != null || piglinAlchemist.getTarget() != null)
                                return piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth() && piglinAlchemist.beltInventory.stream().noneMatch(itemStack -> PotionUtils.getPotion(itemStack) == Potions.STRONG_REGENERATION) && list.size() > 2; // This makes it so alchemists don't
                            // attempt to throw a healing potion if there's only 2 or less of them, as if they did it would make it so only one is attacking while the other is failing to throw the potion because the
                            // attacker would just keep pushing into them
                        }
                    }
                    return piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth();
                }), 1),
                Pair.of(new ThrowPotionAtTargetTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_STRENGTH), LivingEntity::isAlive, (piglin) -> piglin.isAlive() && piglin.getTarget() != null && piglin.getHealth() < (piglin.getMaxHealth() / 2) && !piglin.isHolding((itemStack) -> {
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
                        LivingEntity::isAlive, (piglin) -> piglin.isAlive() && piglin.isOnFire()), 1),
                Pair.of(new ThrowPotionAtSelfTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_REGENERATION),
                        (alchemist) -> alchemist.isAlive() && alchemist.getHealth() < alchemist.getMaxHealth()), 1),
                Pair.of(new ThrowPotionAtSelfTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.FIRE_RESISTANCE),
                        (alchemist) -> alchemist.isAlive() && alchemist.isOnFire()), 1),
                Pair.of(new ThrowPotionAtSelfTask<>(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_HEALING),
                        (alchemist) -> alchemist.isAlive() && alchemist.getHealth() < alchemist.getMaxHealth() && alchemist.beltInventory.stream().noneMatch(itemStack -> PotionUtils.getPotion(itemStack) == Potions.STRONG_REGENERATION)), 2)));
    }

    @Override
    public Optional<? extends LivingEntity> nearestValidAttackTarget(Piglin piglin) {
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
                else {
                    Optional<Player> optional2 = brain.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
                    return optional2.isPresent() && Sensor.isEntityAttackable(piglin, optional2.get()) ? optional2 : Optional.empty();
                }
            }
        }
    }

    @Override
    public SoundEvent soundForActivity(PiglinAlchemist piglin, Activity activity) {
        if (activity == Activity.FIGHT) {
            return PPSounds.ALCHEMIST_ANGRY.get();
        } else if (piglin.isConverting()) {
            return PPSounds.ALCHEMIST_RETREAT.get();
        } else if (activity == Activity.AVOID && isNearAvoidTarget(piglin)) {
            return PPSounds.ALCHEMIST_RETREAT.get();
        } else if (activity == Activity.ADMIRE_ITEM) {
            return PPSounds.ALCHEMIST_ADMIRE.get();
        } else if (activity == Activity.CELEBRATE) {
            return PPSounds.ALCHEMIST_CELEBRATE.get();
        } else if (seesPlayerHoldingLovedItem(piglin)) {
            return PPSounds.ALCHEMIST_JEALOUS.get();
        } else {
            return isNearRepellent(piglin) ? PPSounds.ALCHEMIST_RETREAT.get() : PPSounds.ALCHEMIST_IDLE.get();
        }
    }
}
