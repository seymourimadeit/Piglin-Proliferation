package tallestred.piglinproliferation.common.entities.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;

// Used to fix #50 "When a piglin alchemist zombified, game crashes." by adding a null check to the target getter
public class SafeCrossbowAttack<E extends Mob & CrossbowAttackMob, T extends LivingEntity> extends Behavior<E> {
    private static final int TIMEOUT = 1200;
    private int attackDelay;
    private SafeCrossbowAttack.CrossbowState crossbowState = SafeCrossbowAttack.CrossbowState.UNCHARGED;

    public SafeCrossbowAttack() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 1200);
    }

    protected boolean checkExtraStartConditions(ServerLevel level, E owner) {
        LivingEntity livingentity = getAttackTarget(owner);
        return livingentity != null && owner.isHolding(is -> is.getItem() instanceof CrossbowItem)
                && BehaviorUtils.canSee(owner, livingentity)
                && BehaviorUtils.isWithinAttackRange(owner, livingentity, 0);
    }

    protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
        LivingEntity livingentity = getAttackTarget(entity);
        return entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && livingentity != null && this.checkExtraStartConditions(level, entity);
    }

    protected void tick(ServerLevel level, E owner, long gameTime) {
        LivingEntity livingentity = getAttackTarget(owner);
        if (livingentity == null)
            return;
        this.lookAtTarget(owner, livingentity);
        this.crossbowAttack(owner, livingentity);
    }

    protected void stop(ServerLevel level, E entity, long gameTime) {
        if (entity.isUsingItem()) {
            entity.stopUsingItem();
        }

        if (entity.isHolding(is -> is.getItem() instanceof CrossbowItem)) {
            entity.setChargingCrossbow(false);
            entity.getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }
    }

    private void crossbowAttack(E shooter, LivingEntity target) {
        if (this.crossbowState == SafeCrossbowAttack.CrossbowState.UNCHARGED) {
            shooter.startUsingItem(ProjectileUtil.getWeaponHoldingHand(shooter, item -> item instanceof CrossbowItem));
            this.crossbowState = SafeCrossbowAttack.CrossbowState.CHARGING;
            shooter.setChargingCrossbow(true);
        } else if (this.crossbowState == SafeCrossbowAttack.CrossbowState.CHARGING) {
            if (!shooter.isUsingItem()) {
                this.crossbowState = SafeCrossbowAttack.CrossbowState.UNCHARGED;
            }

            int i = shooter.getTicksUsingItem();
            ItemStack itemstack = shooter.getUseItem();
            if (i >= CrossbowItem.getChargeDuration(itemstack, shooter)) {
                shooter.releaseUsingItem();
                this.crossbowState = SafeCrossbowAttack.CrossbowState.CHARGED;
                this.attackDelay = 20 + shooter.getRandom().nextInt(20);
                shooter.setChargingCrossbow(false);
            }
        } else if (this.crossbowState == SafeCrossbowAttack.CrossbowState.CHARGED) {
            this.attackDelay--;
            if (this.attackDelay == 0) {
                this.crossbowState = SafeCrossbowAttack.CrossbowState.READY_TO_ATTACK;
            }
        } else if (this.crossbowState == SafeCrossbowAttack.CrossbowState.READY_TO_ATTACK) {
            shooter.performRangedAttack(target, 1.0F);
            this.crossbowState = SafeCrossbowAttack.CrossbowState.UNCHARGED;
        }
    }

    private void lookAtTarget(Mob shooter, LivingEntity target) {
        shooter.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
    }

    private static LivingEntity getAttackTarget(LivingEntity shooter) {
        return shooter.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) ? shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get() : null;
    }

    enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;
    }
}
