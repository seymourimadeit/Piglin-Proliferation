package tallestred.piglinproliferation.common.entities.ai.behaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import javax.annotation.Nullable;
import java.util.Map;

public class BowAttack<E extends PiglinAlchemist, T extends LivingEntity> extends Behavior<E> {
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int attackIntervalMin;
    private Path path;
    private int attackTime = -1;
    private int seeTime;
    private int avoidTime;

    public BowAttack(Map<MemoryModuleType<?>, MemoryStatus> map, double speedModifier, float attackRadius, int attackIntervalMin) {
        super(map, 12000);
        this.speedModifier = speedModifier;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.attackIntervalMin = attackIntervalMin;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E alchemist) {
        LivingEntity target = this.getTargetToShootAt(alchemist);
        return alchemist.isHolding(is -> is.getItem() instanceof BowItem) && BehaviorUtils.canSee(alchemist, target);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E alchemist, long gameTime) {
        LivingEntity target = this.getTargetToShootAt(alchemist);
        return target != null && this.checkExtraStartConditions(level, alchemist);
    }

    @Override
    protected void tick(ServerLevel level, E alchemist, long gameTime) {
        LivingEntity target = this.getTargetToShootAt(alchemist);
        if (target != null) {
            double distanceSquared = alchemist.distanceToSqr(target);
            boolean canSee = alchemist.getSensing().hasLineOfSight(target);
            boolean seeTimeGreaterThanZero = this.seeTime > 0;
            alchemist.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
            alchemist.getLookControl().setLookAt(target);
            alchemist.lookAt(target, 30.0f, 30.0F);
            if (alchemist.isUsingItem()) {
                if (!canSee && this.seeTime < -60) {
                    alchemist.stopUsingItem();
                } else if (canSee) {
                    int i = alchemist.getTicksUsingItem();
                    int timeToShoot = distanceSquared <= 40.0D ? Mth.floor(Mth.lerp(distanceSquared / (double) this.attackRadiusSqr, 5.0D, 20.0D)) : 20;
                    if (i >= timeToShoot) {
                        alchemist.stopUsingItem();
                        alchemist.performRangedAttack(target, BowItem.getPowerForTime(i));
                        this.attackTime = this.attackIntervalMin;
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                alchemist.startUsingItem(ProjectileUtil.getWeaponHoldingHand(alchemist, item -> item instanceof BowItem));
            }
            if (distanceSquared > (double) this.attackRadiusSqr && this.seeTime >= 20) {
                this.path = alchemist.getNavigation().createPath(target, 0);
                alchemist.getNavigation().moveTo(this.path, this.speedModifier);
            } else if (distanceSquared < (double) this.attackRadiusSqr && this.seeTime >= 20) {
                alchemist.getNavigation().stop();
            }
            if (canSee != seeTimeGreaterThanZero)
                this.seeTime = 0;
            if (canSee) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }
            if (distanceSquared <= 6.0D || alchemist.getArrowsShot() >= 3) {
                if (this.getTargetToShootAt(alchemist) instanceof AbstractPiglin)
                    return;
                if (this.avoidTime <= 0)
                    this.avoidTime = 60;
                else
                    this.avoidTime -= 10;
                if (alchemist.getArrowsShot() >= 3)
                    alchemist.setArrowsShot(0);
            }
            if (this.avoidTime == 0)
                alchemist.getNavigation().stop();
            if (this.avoidTime < 0)
                this.avoidTime = 0;
            if (this.avoidTime > 60)
                this.avoidTime = 60;
            if (--this.avoidTime > 0) {
                Vec3 vec3 = this.getPosition(alchemist);
                if (distanceSquared <= this.attackRadiusSqr) {
                    if (vec3 != null && alchemist.getNavigation().isDone()) {
                        this.path = alchemist.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
                        alchemist.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(new BlockPos(vec3.x, alchemist.getEyeY(), vec3.z)));
                        if (this.path != null && this.path.canReach()) {
                            alchemist.getNavigation().moveTo(this.path, this.speedModifier);
                            this.attackTime = -1;
                            alchemist.stopUsingItem();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, E alchemist, long gameTime) {
        alchemist.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        alchemist.stopUsingItem();
    }

    @Nullable
    protected Vec3 getPosition(PiglinAlchemist alchemist) {
        if (this.getTargetToShootAt(alchemist) != null)
            return LandRandomPos.getPosAway(alchemist, 5, 7, this.getTargetToShootAt(alchemist).position());
        else
            return LandRandomPos.getPos(alchemist, 5, 7);
    }

    protected LivingEntity getTargetToShootAt(PiglinAlchemist alchemist) {
        return alchemist.getTarget();
    }
}
