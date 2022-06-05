package tallestred.piglinproliferation.common.entities.ai.goals;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import java.util.EnumSet;
import java.util.function.Predicate;

public class ThrowPotionOnSelfGoal extends Goal {
    protected final PiglinAlchemist alchemist;
    protected final ItemStack itemToUse;
    protected final Predicate<? super PiglinAlchemist> canUseSelector;
    protected ItemStack potionToThrow;

    public ThrowPotionOnSelfGoal(PiglinAlchemist alchemist, ItemStack stack, Predicate<? super PiglinAlchemist> pCanUseSelector) {
        this.alchemist = alchemist;
        this.itemToUse = stack;
        this.canUseSelector = pCanUseSelector;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        for (int slot = 0; slot < alchemist.beltInventory.size(); slot++) {
            ItemStack stackInSlot = this.alchemist.beltInventory.get(slot);
            if (stackInSlot.is(itemToUse.getItem()) && PotionUtils.getPotion(itemToUse) == PotionUtils.getPotion(stackInSlot)) {
                this.potionToThrow = stackInSlot;
                return this.canUseSelector.test(this.alchemist);
            }
        }
        return false;
    }

    @Override
    public void start() {
        for (int slot = 0; slot < alchemist.beltInventory.size(); slot++) {
            ItemStack stackInSlot = this.alchemist.beltInventory.get(slot);
            if (!this.alchemist.isGonnaThrowPotion()) {
                if (stackInSlot.is(itemToUse.getItem()) && PotionUtils.getPotion(itemToUse) == PotionUtils.getPotion(stackInSlot) && this.canUseSelector.test(this.alchemist)) {
                    this.potionToThrow = stackInSlot;
                    this.alchemist.setBeltInventorySlot(slot, ItemStack.EMPTY);
                    this.alchemist.setPotionAboutToBeThrown(potionToThrow);
                    this.alchemist.willThrowPotion(true);
                }
            }
        }
    }

    protected void throwPotion(LivingEntity target) {
        this.alchemist.swing(InteractionHand.OFF_HAND);
        Vec3 vec3 = target.getDeltaMovement();
        double d0 = target.getX() + vec3.x - this.alchemist.getX();
        double d1 = target.getEyeY() - (double) 1.1F - this.alchemist.getY();
        double d2 = target.getZ() + vec3.z - this.alchemist.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        ThrownPotion thrownpotion = new ThrownPotion(this.alchemist.level, this.alchemist);
        thrownpotion.setItem(potionToThrow);
        thrownpotion.shootFromRotation(alchemist, alchemist.getXRot(), alchemist.getYRot(), -20.0F, 0.5F, 1.0F);
        if (!this.alchemist.isSilent())
            this.alchemist.level.playSound((Player) null, this.alchemist.getX(), this.alchemist.getY(), this.alchemist.getZ(), SoundEvents.SPLASH_POTION_THROW, this.alchemist.getSoundSource(), 1.0F, 0.8F + this.alchemist.getRandom().nextFloat() * 0.4F);
        this.alchemist.level.addFreshEntity(thrownpotion);
        this.alchemist.willThrowPotion(false);
        this.alchemist.setPotionAboutToBeThrown(ItemStack.EMPTY);
    }

    @Override
    public void stop() {
        if (this.alchemist.isGonnaThrowPotion() && this.alchemist.getPotionAboutToThrown() != null) {
            this.alchemist.willThrowPotion(false);
            for (int slot = 0; slot < alchemist.beltInventory.size(); slot++) {
                ItemStack stackInSlot = this.alchemist.beltInventory.get(slot);
                if (stackInSlot.isEmpty()) {
                    this.alchemist.beltInventory.set(slot, this.alchemist.getPotionAboutToThrown().copy());
                    this.alchemist.setPotionAboutToBeThrown(ItemStack.EMPTY);
                }
            }
        }
    }
}
