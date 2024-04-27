package tallestred.piglinproliferation.common.entities.ai.behaviors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import tallestred.piglinproliferation.PPActivities;
import tallestred.piglinproliferation.PPMemoryModules;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import java.util.List;
import java.util.function.Predicate;

public class ShootTippedArrow extends BowAttack<PiglinAlchemist, LivingEntity> {
    protected final Predicate<? super AbstractPiglin> nearbyPiglinPredicate;
    private final ItemStack itemToUse;
    private AbstractPiglin piglinToTarget;

    public ShootTippedArrow(double speedModifier, float attackRadius, int attackIntervalMin, ItemStack item, Predicate<? super AbstractPiglin> nearbyPiglinPredicate) {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,  MemoryStatus.REGISTERED, PPMemoryModules.POTION_THROW_TARGET.get(), MemoryStatus.VALUE_ABSENT), speedModifier, attackRadius, attackIntervalMin);
        this.nearbyPiglinPredicate = nearbyPiglinPredicate;
        this.itemToUse = item;
    }

    @Override
    protected LivingEntity getTargetToShootAt(PiglinAlchemist alchemist) {
        List<AbstractPiglin> list = alchemist.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
        if (!list.isEmpty()) {
            for (AbstractPiglin piglin : list) {
                    piglinToTarget = piglin;
                    for (MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(itemToUse)) {
                        List<ItemStack> filteredList = alchemist.beltInventory.getMatches(itemStack -> itemStack.is((itemToUse.getItem())));
                        for (ItemStack item : filteredList) {
                            if (PotionUtils.getPotion(item) == PotionUtils.getPotion(itemToUse)) {
                                boolean hasArrow = alchemist.getItemShownOnOffhand().is(itemToUse.getItem()) || alchemist.beltInventory.getMatches(itemStack -> itemStack.is((itemToUse.getItem()))) != null;
                                return hasArrow && this.nearbyPiglinPredicate.test(piglinToTarget) && !piglinToTarget.hasEffect(mobeffectinstance.getEffect()) ? piglinToTarget : null;
                            }
                        }
                    }
                }
            }
        return null;
    }

    @Override
    protected void start(ServerLevel level, PiglinAlchemist alchemist, long gameTime) {
        alchemist.getBrain().setMemory(PPMemoryModules.POTION_THROW_TARGET.get(), piglinToTarget);
        alchemist.getBrain().setActiveActivityIfPossible(PPActivities.THROW_POTION_ACTIVITY.get());
    }

    @Override
    protected void stop(ServerLevel level, PiglinAlchemist alchemist, long gameTime) {
        super.stop(level, alchemist, gameTime);
        if (alchemist.getItemShownOnOffhand().getItem() instanceof ArrowItem) {
            for (int slot = 0; slot < alchemist.beltInventory.size(); slot++) {
                ItemStack stackInSlot = alchemist.beltInventory.get(slot);
                if (stackInSlot.isEmpty()) {
                    alchemist.beltInventory.set(slot, alchemist.getItemShownOnOffhand().copy());
                    alchemist.setItemShownOnOffhand(ItemStack.EMPTY);
                }
            }
        }
        alchemist.getBrain().eraseMemory(PPMemoryModules.POTION_THROW_TARGET.get());
    }
}
