package tallestred.piglinproliferation.common.entities.ai.goals;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import javax.annotation.Nullable;
import java.util.EnumSet;

//less strafing
public class AlchemistBowAttackGoal<T extends PiglinAlchemist> extends Goal {
    protected final T mob;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int attackIntervalMin;
    private int attackTime = -1;
    private int seeTime;
    private int avoidTime;

    public AlchemistBowAttackGoal(T pMob, double pSpeedModifier, int pAttackIntervalMin, float pAttackRadius) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.attackIntervalMin = pAttackIntervalMin;
        this.attackRadiusSqr = pAttackRadius * pAttackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }


    @Override
    public boolean canUse() {
        return this.getTargetToShootAt() != null && this.isHoldingBow();
    }

    protected boolean isHoldingBow() {
        return this.mob.isHolding(is -> is.getItem() instanceof BowItem);
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingBow() && this.mob.getArrowsShot() < 3;
    }

    protected LivingEntity getTargetToShootAt() {
        return this.mob.getTarget();
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.mob.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * Basically
     * > stay as far from player as possible
     * > if enemy is too close start quick drawing arrows and run away to somewhere further
     * > will cancel when its shot an arrow three times in order to reposition itself
     */
    @Override
    public void tick() {
        LivingEntity livingentity = this.getTargetToShootAt();
        if (livingentity != null) {
            double distanceSquared = this.mob.distanceToSqr(livingentity);
            boolean canSee = this.mob.getSensing().hasLineOfSight(livingentity);
            boolean seeTimeGreaterThanZero = this.seeTime > 0;
            this.mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.getTargetToShootAt(), true));
            this.mob.getLookControl().setLookAt(livingentity);
            this.mob.lookAt(livingentity, 30.0f, 30.0F);
            if (this.mob.isUsingItem()) {
                if (!canSee && this.seeTime < -60) {
                    this.mob.stopUsingItem();
                } else if (canSee) {
                    int i = this.mob.getTicksUsingItem();
                    int timeToShoot = Mth.floor(Mth.lerp(distanceSquared / (double) this.attackRadiusSqr, 5.0D, 20.0D));
                    if (i >= timeToShoot) {
                        this.mob.stopUsingItem();
                        this.mob.performRangedAttack(livingentity, BowItem.getPowerForTime(i));
                        this.attackTime = this.attackIntervalMin;
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
            }
            if (distanceSquared > (double) this.attackRadiusSqr && this.seeTime >= 20) {
                WalkTarget walktarget = new WalkTarget(livingentity, (float) this.speedModifier, 0);
                this.mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walktarget);
            } else if (distanceSquared < (double) this.attackRadiusSqr && this.seeTime >= 20) {
                this.mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                this.mob.getNavigation().stop();
            }


            if (canSee != seeTimeGreaterThanZero)
                this.seeTime = 0;
            if (canSee) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }
            if (distanceSquared <= 6.0D) {
                if (this.avoidTime <= 0)
                    this.avoidTime = 60;
                else
                    this.avoidTime += 2;
            }

            if (this.avoidTime < 0)
                this.avoidTime = 0;
            if(this.avoidTime > 60)
                this.avoidTime = 60;
            if (--this.avoidTime > 0) {
                this.attackTime = -1;
                Vec3 vec3 = this.getPosition();
                if ((mob.getNavigation().isDone() || mob.getNavigation().isStuck()) || this.avoidTime == 0)
                    this.mob.getNavigation().stop();
                if (vec3 != null)
                    this.mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, 1.5F, 4));
            }
        }
    }

    @Nullable
    protected Vec3 getPosition() {
        if (this.getTargetToShootAt() != null)
            return LandRandomPos.getPosAway(this.mob, 10, 7, this.getTargetToShootAt().position());
        else
            return  LandRandomPos.getPos(this.mob, 10, 7);
    }
}
