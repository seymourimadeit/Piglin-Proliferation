package tallestred.piglinproliferation.common.entities.ai.goals;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import java.util.List;
import java.util.function.Predicate;

public class HelpAlliesWithTippedArrowGoal extends AlchemistBowAttackGoal {
    protected final Predicate<? super AbstractPiglin> nearbyPiglinPredicate;
    private final ItemStack itemToUse;
    private AbstractPiglin piglinToTarget;

    public HelpAlliesWithTippedArrowGoal(PiglinAlchemist pMob, double pSpeedModifier, int pAttackIntervalMin, float pAttackRadius, ItemStack item, Predicate<? super AbstractPiglin> nearbyPiglinPredicate) {
        super(pMob, pSpeedModifier, pAttackIntervalMin, pAttackRadius);
        this.nearbyPiglinPredicate = nearbyPiglinPredicate;
        this.itemToUse = item;
    }

    @Override
    protected LivingEntity getTargetToShootAt() {
        List<AbstractPiglin> list = this.mob.level.getEntitiesOfClass(AbstractPiglin.class, this.mob.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
        if (!list.isEmpty()) {
            for (AbstractPiglin piglin : list) {
                if (piglin != mob) {
                    piglinToTarget = piglin;
                    for (MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(itemToUse)) {
                        List<ItemStack> filteredList = this.mob.beltInventory.stream().filter(itemStack -> itemStack.is((itemToUse.getItem()))).toList();
                        for (ItemStack item : filteredList) {
                            if (PotionUtils.getPotion(item) == PotionUtils.getPotion(itemToUse)) {
                                boolean hasArrow = this.mob.getItemShownOnOffhand().is(itemToUse.getItem()) || this.mob.beltInventory.stream().filter(itemStack -> itemStack.is((itemToUse.getItem()))).toList() != null;
                                return hasArrow && this.nearbyPiglinPredicate.test(piglinToTarget) && !piglinToTarget.hasEffect(mobeffectinstance.getEffect()) ? piglinToTarget : null;
                            }
                        }
                    }

                }
            }
        }
        return null;
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingBow();
    }

    @Override
    public void stop() {
        super.stop();
        if (this.mob.getItemShownOnOffhand().getItem() instanceof ArrowItem) {
            for (int slot = 0; slot < this.mob.beltInventory.size(); slot++) {
                ItemStack stackInSlot = this.mob.beltInventory.get(slot);
                if (stackInSlot.isEmpty()) {
                    this.mob.beltInventory.set(slot, this.mob.getItemShownOnOffhand().copy());
                    this.mob.setItemShownOnOffhand(ItemStack.EMPTY);
                }
            }
        }
    }
}
