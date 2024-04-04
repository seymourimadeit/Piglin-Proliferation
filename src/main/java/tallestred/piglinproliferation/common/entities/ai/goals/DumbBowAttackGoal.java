package tallestred.piglinproliferation.common.entities.ai.goals;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.EnumSet;

public class DumbBowAttackGoal<T extends Mob> extends Goal {
    private final T mob;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int attackIntervalMin;
    private int attackTime = -1;
    private int seeTime;
    public <M extends Monster> DumbBowAttackGoal(M pMob, double pSpeedModifier, int pAttackIntervalMin, float pAttackRadius) {
        this((T) pMob, pSpeedModifier, pAttackIntervalMin, pAttackRadius);
    }

    public DumbBowAttackGoal(T pMob, double pSpeedModifier, int pAttackIntervalMin, float pAttackRadius) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.attackIntervalMin = pAttackIntervalMin;
        this.attackRadiusSqr = pAttackRadius * pAttackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
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
        return (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingBow();
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

    @Override
    public void tick() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null) {
            double distance = this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
            boolean flag = this.mob.getSensing().hasLineOfSight(livingentity);
            boolean flag1 = this.seeTime > 0;
            if (flag != flag1) {
                this.seeTime = 0;
            }

            if (flag) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }
            if (!(distance > (double) this.attackRadiusSqr) && this.seeTime >= 20) {
                this.mob.getNavigation().stop();
            } else {
                this.mob.getNavigation().moveTo(livingentity, this.speedModifier);
            }
            Entity entity = this.mob.getControlledVehicle();
            if (entity instanceof Mob) {
                Mob mob = (Mob) entity;
                mob.lookAt(livingentity, 30.0F, 30.0F);
                mob.getLookControl().setLookAt(livingentity);
            }
            this.mob.lookAt(livingentity, 30.0F, 30.0F);
            this.mob.getLookControl().setLookAt(livingentity);
            this.mob.getNavigation().moveTo(livingentity, 1.0D);
            if (this.mob.isUsingItem()) {
                if (!flag && this.seeTime < -60) {
                    this.mob.stopUsingItem();
                } else if (flag) {
                    int i = this.mob.getTicksUsingItem();
                    if (i >= 20) {
                        this.mob.stopUsingItem();
                        ItemStack itemstack = this.mob.getProjectile(this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem)));
                        AbstractArrow abstractarrowentity = ProjectileUtil.getMobArrow(this.mob, itemstack, BowItem.getPowerForTime(i));
                        abstractarrowentity = ((net.minecraft.world.item.BowItem) this.mob.getMainHandItem().getItem()).customArrow(abstractarrowentity, itemstack);
                        int powerLevel = itemstack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
                        if (powerLevel > 0)
                            abstractarrowentity
                                    .setBaseDamage(abstractarrowentity.getBaseDamage() + (double) powerLevel * 0.5D + 0.5D);
                        int punchLevel = itemstack.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
                        if (punchLevel > 0)
                            abstractarrowentity.setKnockback(punchLevel);
                        if (itemstack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0)
                            abstractarrowentity.setSecondsOnFire(100);
                        double d0 = livingentity.getX() - this.mob.getX();
                        double d1 = livingentity.getY(0.3333333333333333D) - abstractarrowentity.getY();
                        double d2 = livingentity.getZ() - this.mob.getZ();
                        double d3 = Mth.sqrt((float) (d0 * d0 + d2 * d2));
                        abstractarrowentity.shoot(d0, d1 + d3 * (double) 0.2F, d2, 1.6F,
                                (float) (14 - this.mob.level().getDifficulty().getId() * 4));
                        this.mob.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.mob.getRandom().nextFloat() * 0.4F + 0.8F));
                        this.mob.level().addFreshEntity(abstractarrowentity);
                        itemstack.shrink(1);
                        this.attackTime = this.attackIntervalMin;
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
            }

        }
    }
}
