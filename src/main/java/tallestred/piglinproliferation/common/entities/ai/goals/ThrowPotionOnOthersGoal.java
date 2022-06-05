package tallestred.piglinproliferation.common.entities.ai.goals;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import java.util.List;
import java.util.function.Predicate;

public class ThrowPotionOnOthersGoal extends ThrowPotionOnSelfGoal {
    protected final Predicate<? super AbstractPiglin> nearbyPiglinPredicate;
    protected AbstractPiglin nearbyPiglins;
    private int ticksUntilThrow;


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
            return this.nearbyPiglinPredicate.test(nearbyPiglins) && !nearbyPiglins.hasEffect(mobeffectinstance.getEffect()) && this.ticksUntilThrow > 0;
        }
        return false;
    }


    @Override
    public void start() {
        super.start();
        if (this.ticksUntilThrow <= 0)
            this.ticksUntilThrow = 20;
    }

    @Override
    public void tick() {
        this.alchemist.lookAt(nearbyPiglins, 30.0F, 30.0F);
        BehaviorUtils.lookAtEntity(alchemist, nearbyPiglins);
        BehaviorUtils.lookAtEntity(nearbyPiglins, alchemist);
        this.alchemist.getLookControl().setLookAt(nearbyPiglins);
        this.nearbyPiglins.lookAt(alchemist, 30.0F, 30.0F);
        this.nearbyPiglins.getLookControl().setLookAt(alchemist);
        if (this.alchemist.distanceTo(nearbyPiglins) > 5.0D) this.alchemist.getNavigation().moveTo(nearbyPiglins, 1.0D);
        else this.alchemist.getNavigation().stop();
        --this.ticksUntilThrow;
        if (this.ticksUntilThrow <= 0) {
            this.throwPotion(nearbyPiglins);
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.ticksUntilThrow = 0;
    }
}
