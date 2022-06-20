package tallestred.piglinproliferation.common.entities.ai.goals;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class ThrowPotionOnSelfGoal extends BaseAlchemistThrowPotionGoal {
    protected int ticksUntilThrow;
    protected int panicTicks;

    public ThrowPotionOnSelfGoal(PiglinAlchemist alchemist, ItemStack stack, Predicate<? super PiglinAlchemist> pCanUseSelector) {
        super(alchemist, stack, pCanUseSelector);
    }

    @Override
    public boolean canUse() {
        for (int slot = 0; slot < alchemist.beltInventory.size(); slot++) {
            ItemStack stackInSlot = this.alchemist.beltInventory.get(slot);
            if (stackInSlot.is(itemToUse.getItem()) && PotionUtils.getPotion(itemToUse) == PotionUtils.getPotion(stackInSlot)) {
                this.potionToThrow = stackInSlot;
                for (MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(itemToUse)) {
                    return super.canUse() && !this.alchemist.hasEffect(mobeffectinstance.getEffect());
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        for (MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(itemToUse)) {
            return this.canUseSelector.test(alchemist) && !alchemist.hasEffect(mobeffectinstance.getEffect()) && (this.ticksUntilThrow > 0);
        }
        return false;
    }

    @Override
    public void start() {
        super.start();
        if (this.ticksUntilThrow <= 0)
            this.ticksUntilThrow = 15;
    }

    @Override
    public void tick() {
        List<LivingEntity> list = this.alchemist.level.getEntitiesOfClass(LivingEntity.class, this.alchemist.getBoundingBox().inflate(5.0D, 3.0D, 5.0D));
        if (!list.isEmpty()) {
            for (LivingEntity entity : list) {
                if (alchemist.getTarget() != null && alchemist.getTarget() == entity || entity instanceof Mob && (((Mob) entity).getTarget() != null && (((Mob) entity).getTarget() == alchemist))) {
                    if (entity.distanceTo(alchemist) <= 4.0D) {
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
        if (this.ticksUntilThrow == 5)
            this.alchemist.playSound(PPSounds.ALCHEMIST_ABOUT_TO_THROW_POTION.get(), 1.0F, 1.0F);
        if (--this.ticksUntilThrow <= 0) {
            this.throwPotion();
        }
        this.alchemist.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.alchemist.blockPosition()));
    }

    @Override
    public void stop() {
        super.stop();
        this.ticksUntilThrow = 0;
    }

    @Nullable
    protected Vec3 getPosition() {
        if (this.alchemist.getTarget() != null)
            return LandRandomPos.getPosAway(this.alchemist, 8, 7, this.alchemist.getTarget().position());
        else
            return LandRandomPos.getPos(this.alchemist, 8, 7);
    }
}
