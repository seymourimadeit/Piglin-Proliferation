package tallestred.piglinproliferation.common.entities.ai.behaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.PPActivities;
import tallestred.piglinproliferation.PPMemoryModules;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

import static tallestred.piglinproliferation.util.CodeUtilities.potionContents;

public class ThrowPotionAtTargetTask<E extends PiglinAlchemist> extends BaseThrowPotion<E> {
    protected final Predicate<Mob> nearbyPiglinPredicate;

    public ThrowPotionAtTargetTask(ItemStack stack, Predicate<PiglinAlchemist> pCanUseSelector, Predicate<Mob> nearbyPiglinPredicate) {
        super(stack, pCanUseSelector);
        this.nearbyPiglinPredicate = nearbyPiglinPredicate;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E alchemist) {
        List<AbstractPiglin> list = alchemist.level().getEntitiesOfClass(AbstractPiglin.class, alchemist.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
        if (!list.isEmpty()) {
            for (AbstractPiglin piglin : list) {
                if (piglin != alchemist) {
                    for (MobEffectInstance mobeffectinstance : potionContents(itemToUse).getAllEffects()) {
                        List<AbstractPiglin> listOfAlchemists = list.stream().filter(abstractPiglin -> abstractPiglin != alchemist
                                && abstractPiglin instanceof PiglinAlchemist).toList();
                        if (piglin != null && alchemist.hasLineOfSight(piglin)
                                && listOfAlchemists.stream().noneMatch(abstractPiglin -> ((PiglinAlchemist) abstractPiglin).isGonnaThrowPotion()) &&
                                super.checkExtraStartConditions(level, alchemist)
                                && this.nearbyPiglinPredicate.test(piglin) && !piglin.hasEffect(mobeffectinstance.getEffect())) {
                            if (piglin.getTarget() != null && listOfAlchemists.size() < 2 && alchemist.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty()).find(livingEntity -> (livingEntity instanceof Mob && ((Mob) livingEntity).getTarget() instanceof AbstractPiglin || livingEntity.getLastHurtMob() instanceof AbstractPiglin)).toList().size() > 1) {
                                return false;
                            } else {
                                alchemist.getBrain().setMemory(PPMemoryModules.POTION_THROW_TARGET.get(), piglin);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E alchemist, long gameTime) {
        if (alchemist.getBrain().hasMemoryValue((PPMemoryModules.POTION_THROW_TARGET.get()))) {
            Mob throwTarget = alchemist.getBrain().getMemory(PPMemoryModules.POTION_THROW_TARGET.get()).get();
            for (MobEffectInstance mobeffectinstance : potionContents(itemToUse).getAllEffects()) {
                return throwTarget != null && alchemist.hasLineOfSight(throwTarget) && this.nearbyPiglinPredicate.test(throwTarget) && !throwTarget.hasEffect(mobeffectinstance.getEffect()) && this.ticksUntilThrow > 0;
            }
            return throwTarget != null && alchemist.hasLineOfSight(throwTarget);
        } else {
            return false;
        }
    }

    @Override
    protected void start(ServerLevel level, E alchemist, long gameTime) {
        if (alchemist.getBrain().hasMemoryValue((PPMemoryModules.POTION_THROW_TARGET.get()))) {
            super.start(level, alchemist, gameTime);
            alchemist.getBrain().setActiveActivityIfPossible(PPActivities.THROW_POTION_ACTIVITY.get());
            Mob throwTarget = alchemist.getBrain().getMemory(PPMemoryModules.POTION_THROW_TARGET.get()).get();
            if (throwTarget == null)
                return;
            if (this.ticksUntilThrow <= 0)
                this.ticksUntilThrow = 20;
        }
    }

    @Override
    protected void stop(ServerLevel level, E alchemist, long gameTime) {
        super.stop(level, alchemist, gameTime);
        alchemist.getBrain().eraseMemory(PPMemoryModules.POTION_THROW_TARGET.get());
        this.ticksUntilThrow = 0;
        this.panicTicks = 0;
    }

    @Override
    protected void tick(ServerLevel level, E alchemist, long gameTime) {
        Mob throwTarget = alchemist.getBrain().getMemory(PPMemoryModules.POTION_THROW_TARGET.get()).orElseGet(null);
        if (throwTarget == null)
            return;
        alchemist.lookAt(throwTarget, 30.0F, 30.0F);
        BehaviorUtils.lookAtEntity(alchemist, throwTarget);
        BehaviorUtils.lookAtEntity(throwTarget, alchemist);
        alchemist.getLookControl().setLookAt(throwTarget);
        if (alchemist.distanceTo(throwTarget) > 5.0D)
            alchemist.getNavigation().moveTo(throwTarget, 1.0D);
        else alchemist.getNavigation().stop();
        List<LivingEntity> list = alchemist.level().getEntitiesOfClass(LivingEntity.class, alchemist.getBoundingBox().inflate(5.0D, 3.0D, 5.0D));
        if (!list.isEmpty()) {
            for (LivingEntity entity : list) {
                if (throwTarget.getTarget() != null && throwTarget.getTarget() == entity || entity instanceof Mob && (((Mob) entity).getTarget() != null && (((Mob) entity).getTarget() == alchemist || ((Mob) entity).getTarget() == throwTarget))) {
                    if (entity.distanceTo(throwTarget) <= 4.0D || entity.distanceTo(alchemist) <= 4.0D) {
                        if (this.panicTicks <= 0)
                            this.panicTicks = 5;
                        this.ticksUntilThrow += 5;
                        if (--this.panicTicks > 0) {
                            Vec3 vec3 = this.getPosition(alchemist);
                            if (vec3 != null && alchemist.getNavigation().isDone()) {
                                alchemist.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1.5D);
                            }
                        }
                    }
                }
            }
        }
        if (this.ticksUntilThrow > 20)
            this.ticksUntilThrow = 20;
        if (this.panicTicks < 0)
            this.panicTicks = 0;
        if (this.panicTicks >= 15)
            this.panicTicks = 15;
        if (!alchemist.hasLineOfSight(throwTarget))
            this.ticksUntilThrow += 5;
        if (this.ticksUntilThrow == 5)
            alchemist.playSound(PPSounds.ALCHEMIST_ABOUT_TO_THROW_POTION.get(), 1.0F, 1.0F);
        if (--this.ticksUntilThrow <= 0) {
            this.throwPotion(alchemist);
        }
    }

    @Nullable
    protected Vec3 getPosition(PiglinAlchemist alchemist) {
        if (alchemist.getTarget() != null)
            return LandRandomPos.getPosAway(alchemist, 8, 7, alchemist.getTarget().position());
        else
            return LandRandomPos.getPos(alchemist, 8, 7);
    }

}
