package tallestred.piglinproliferation.common.entities.ai.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import javax.annotation.Nullable;
import java.util.EnumSet;

//less strafing
public class AlchemistBowAttackGoal<T extends PiglinAlchemist> extends Goal {
    private final T mob;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int attackIntervalMin;
    private int attackTime = -1;
    private int seeTime;
    private boolean runSomewhere;

    public AlchemistBowAttackGoal(T pMob, double pSpeedModifier, int pAttackIntervalMin, float pAttackRadius) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.attackIntervalMin = pAttackIntervalMin;
        this.attackRadiusSqr = pAttackRadius * pAttackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public void setMinAttackInterval(int pAttackCooldown) {
        this.attackIntervalMin = pAttackCooldown;
    }


    @Override
    public boolean canUse() {
        return this.mob.getTarget() == null ? false : this.isHoldingBow();
    }

    protected boolean isHoldingBow() {
        return this.mob.isHolding(is -> is.getItem() instanceof BowItem);
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingBow() && this.mob.getArrowsShot() < 3;
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

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null) {
            this.mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.mob.getTarget(), true));
            double distanceSquared = this.mob.distanceToSqr(livingentity);
            boolean canSee = this.mob.getSensing().hasLineOfSight(livingentity);
            boolean seeTimeGreaterThanZero = this.seeTime > 0;
            if (canSee != seeTimeGreaterThanZero)
                this.seeTime = 0;
            if (canSee) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }
            if (distanceSquared <= 4.0D && !runSomewhere) {
                this.runSomewhere = true;
            }
            if (this.runSomewhere) {
                this.mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.mob.getTarget(), true));
                this.attackTime = -1;
                this.mob.stopUsingItem();
                Vec3 vec3 = this.getPosition();
                if (vec3 != null) {
                    this.mob.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1.5D);
                }
                if (distanceSquared >= (double) this.attackRadiusSqr || mob.getNavigation().isDone())
                    this.runSomewhere = false;

            }
            if (distanceSquared < (double) this.attackRadiusSqr && this.seeTime >= 20 && !this.runSomewhere)
                this.mob.getNavigation().stop();
            if (this.mob.isUsingItem()) {
                if (!canSee && this.seeTime < -60) {
                    this.mob.stopUsingItem();
                } else if (canSee) {
                    int i = this.mob.getTicksUsingItem();
                    if (i >= 20) {
                        this.mob.stopUsingItem();
                        this.mob.performRangedAttack(livingentity, BowItem.getPowerForTime(i));
                        this.attackTime = this.attackIntervalMin;
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60 && !runSomewhere) {
                this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
            }

        }
    }

    @Nullable
    protected Vec3 getPosition() {
        return DefaultRandomPos.getPosAway(this.mob, 16, 7, this.mob.getTarget().position());
    }
}
