package tallestred.piglinproliferation.common.entities.ai.goals;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class ThrowPotionOnOthersGoal extends BaseAlchemistThrowPotionGoal {
    protected final Predicate<? super AbstractPiglin> nearbyPiglinPredicate;
    protected AbstractPiglin nearbyPiglins;
    private int ticksUntilThrow;
    private int panicTicks;


    public ThrowPotionOnOthersGoal(PiglinAlchemist alchemist, ItemStack stack, Predicate<? super PiglinAlchemist> pCanUseSelector, Predicate<? super AbstractPiglin> nearbyPiglinPredicate) {
        super(alchemist, stack, pCanUseSelector);
        this.nearbyPiglinPredicate = nearbyPiglinPredicate;
    }

    @Override
    public boolean canUse() {
        List<AbstractPiglin> list = this.alchemist.level.getEntitiesOfClass(AbstractPiglin.class, this.alchemist.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
        if (!list.isEmpty()) {
            for (AbstractPiglin piglin : list) {
                if (piglin != alchemist) {
                    nearbyPiglins = piglin;
                    for (MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(itemToUse)) {
                        return super.canUse() && this.nearbyPiglinPredicate.test(nearbyPiglins) && !nearbyPiglins.hasEffect(mobeffectinstance.getEffect());
                    }
                }

            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        for (MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(itemToUse)) {
            return nearbyPiglins != null && this.alchemist.hasLineOfSight(nearbyPiglins) && this.nearbyPiglinPredicate.test(nearbyPiglins) && !nearbyPiglins.hasEffect(mobeffectinstance.getEffect()) && this.ticksUntilThrow > 0;
        }
        return this.alchemist.hasLineOfSight(nearbyPiglins);
    }


    @Override
    public void start() {
        super.start();
        if (nearbyPiglins == null)
            return;
        if (this.ticksUntilThrow <= 0)
            this.ticksUntilThrow = 20;
    }

    @Override
    public void tick() {
        if (nearbyPiglins == null)
            return;
        this.alchemist.lookAt(nearbyPiglins, 30.0F, 30.0F);
        BehaviorUtils.lookAtEntity(alchemist, nearbyPiglins);
        BehaviorUtils.lookAtEntity(nearbyPiglins, alchemist);
        this.alchemist.getLookControl().setLookAt(nearbyPiglins);
        this.nearbyPiglins.lookAt(alchemist, 30.0F, 30.0F);
        this.nearbyPiglins.getLookControl().setLookAt(alchemist);
        if (this.alchemist.distanceTo(nearbyPiglins) > 5.0D) this.alchemist.getNavigation().moveTo(nearbyPiglins, 1.0D);
        else this.alchemist.getNavigation().stop();
        List<LivingEntity> list = this.alchemist.level.getEntitiesOfClass(LivingEntity.class, this.alchemist.getBoundingBox().inflate(5.0D, 3.0D, 5.0D));
        if (!list.isEmpty()) {
            for (LivingEntity entity : list) {
                if (nearbyPiglins.getTarget() != null && nearbyPiglins.getTarget() == entity || entity instanceof Mob && (((Mob) entity).getTarget() != null && (((Mob) entity).getTarget() == alchemist || ((Mob) entity).getTarget() == nearbyPiglins))) {
                    if (entity.distanceTo(nearbyPiglins) <= 4.0D || entity.distanceTo(alchemist) <= 4.0D) {
                        if (this.panicTicks <= 0)
                            this.panicTicks = 5;
                        this.ticksUntilThrow += 5;
                        if (--this.panicTicks > 0) {
                            Vec3 vec3 = this.getPosition();
                            if (vec3 != null) {
                                this.alchemist.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1.5D);
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
        if (!this.alchemist.hasLineOfSight(nearbyPiglins))
            this.ticksUntilThrow += 5;
        if (this.ticksUntilThrow == 5)
            this.alchemist.playSound(PPSounds.ALCHEMIST_ABOUT_TO_THROW_POTION.get(), 1.0F, 1.0F);
        if (--this.ticksUntilThrow <= 0) {
            this.throwPotion();
        }
    }

    @Nullable
    protected Vec3 getPosition() {
        if (this.alchemist.getTarget() != null)
            return LandRandomPos.getPosAway(this.alchemist, 8, 7, this.alchemist.getTarget().position());
        else
            return LandRandomPos.getPos(this.alchemist, 8, 7);
    }

    @Override
    public void stop() {
        super.stop();
        this.ticksUntilThrow = 0;
    }
}