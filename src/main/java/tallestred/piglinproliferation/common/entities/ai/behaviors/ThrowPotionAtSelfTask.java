package tallestred.piglinproliferation.common.entities.ai.behaviors;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.PPActivities;
import tallestred.piglinproliferation.PPMemoryModules;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class ThrowPotionAtSelfTask<E extends PiglinAlchemist> extends BaseThrowPotion<E> {
    public ThrowPotionAtSelfTask(ItemStack stack, Predicate<PiglinAlchemist> pCanUseSelector) {
        super(stack, pCanUseSelector);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E alchemist) {
        for (MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(itemToUse)) {
            if (super.checkExtraStartConditions(level, alchemist)) {
                List<AbstractPiglin> list = alchemist.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
                if (!alchemist.hasEffect(mobeffectinstance.getEffect())) {
                    if (alchemist.getTarget() != null) {
                        return list.size() > 2; // Make sure I have people backing me up if I have to throw a potion and theres someone attacking me
                    } else {
                        return !list.stream().filter(abstractPiglin -> abstractPiglin instanceof PiglinAlchemist && ((PiglinAlchemist)abstractPiglin).isGonnaThrowPotion()).findAny().isPresent();
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void tick(ServerLevel level, E alchemist, long gameTIme) {
        BlockPos lookPos = new BlockPos((int) alchemist.position().x, (int) (alchemist.getBoundingBox().minY - 0.5000001D), (int) alchemist.position().z);
        alchemist.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(lookPos));
        List<LivingEntity> list = alchemist.level.getEntitiesOfClass(LivingEntity.class, alchemist.getBoundingBox().inflate(5.0D, 3.0D, 5.0D));
        if (!list.isEmpty()) {
            for (LivingEntity entity : list) {
                if (entity != alchemist) {
                    if (alchemist.getTarget() != null && alchemist.getTarget() == entity || entity instanceof Mob && (((Mob) entity).getTarget() != null && (((Mob) entity).getTarget() == alchemist) || entity.getLastHurtMob() != null && entity.getLastHurtMob() instanceof AbstractPiglin)) {
                        if (entity.distanceTo(alchemist) <= 4.0D || alchemist.distanceTo(entity) <= 4.0D) {
                            if (this.panicTicks <= 0)
                                this.panicTicks = 20;
                            this.ticksUntilThrow += 5;
                        }
                    }
                }
            }
        }
        if (this.ticksUntilThrow > 20)
            this.ticksUntilThrow = 20;
        if (this.panicTicks >= 15)
            this.panicTicks = 15;
        if (--this.panicTicks > 0) {
            Vec3 vec3 = this.getPosition(alchemist);
            if (vec3 != null && alchemist.getNavigation().isDone()) {
                alchemist.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1.5D);
            }
        }
        alchemist.setYRot(Mth.rotateIfNecessary(alchemist.getYRot(), alchemist.yHeadRot, 0.0F));
        alchemist.setXRot(Mth.rotateIfNecessary(alchemist.getXRot(), alchemist.getMaxHeadXRot(), 0.0F));
        if (this.ticksUntilThrow == 5)
            alchemist.playSound(PPSounds.ALCHEMIST_ABOUT_TO_THROW_POTION.get(), 1.0F, 1.0F);
        if (--this.ticksUntilThrow <= 0 && this.panicTicks <= 0) {
            alchemist.setYHeadRot(-200.0F);
            alchemist.setXRot(90.0F);
            this.throwPotion(alchemist);
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E alchemist, long gameTime) {
        List<AbstractPiglin> list = alchemist.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
        if (alchemist.getTarget() != null) {
            return list.size() > 2; // Make sure I have people backing me up if I have to throw a potion and theres someone attacking me
        } else {
            for (MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(itemToUse)) {
                return this.canUseSelector.test(alchemist) && !alchemist.hasEffect(mobeffectinstance.getEffect()) && this.ticksUntilThrow > 0;
            }
        }
        return !list.stream().filter(abstractPiglin -> abstractPiglin instanceof PiglinAlchemist && ((PiglinAlchemist)abstractPiglin).isGonnaThrowPotion()).findAny().isPresent();
    }

    @Override
    protected void start(ServerLevel level, E alchemist, long gameTime) {
        super.start(level, alchemist, gameTime);
        alchemist.getBrain().setMemory(PPMemoryModules.POTION_THROW_TARGET.get(), alchemist);
        alchemist.getBrain().setActiveActivityIfPossible(PPActivities.THROW_POTION_ACTIVITY.get());
        if (this.ticksUntilThrow <= 0)
            this.ticksUntilThrow = 15;
    }

    @Override
    protected void stop(ServerLevel level, E alchemist, long gameTime) {
        super.stop(level, alchemist, gameTime);
        this.ticksUntilThrow = 0;
        this.panicTicks = 0;
        alchemist.getBrain().eraseMemory(PPMemoryModules.POTION_THROW_TARGET.get());
    }

    @Nullable
    protected Vec3 getPosition(PiglinAlchemist alchemist) {
        return LandRandomPos.getPos(alchemist, 8, 7);
    }
}
