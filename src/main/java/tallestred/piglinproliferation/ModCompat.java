package tallestred.piglinproliferation;

import com.infamous.sapience.SapienceConfig;
import com.infamous.sapience.util.GreedHelper;
import com.infamous.sapience.util.PiglinReputationType;
import com.infamous.sapience.util.PiglinTasksHelper;
import com.infamous.sapience.util.ReputationHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.common.PPLootTables;
import tallestred.piglinproliferation.common.entities.ai.PiglinAlchemistAi;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.infamous.sapience.util.PiglinTasksHelper.*;

public class ModCompat {
    /**
     * @author infamous
     */
    public static void stopHoldingOffHandItem(Piglin piglin, boolean doBarter) {
        Entity interactor = ReputationHelper.getPreviousInteractor(piglin);
        boolean willDropLoot = willDropLootFor(piglin, interactor);
        ItemStack offHandItem = piglin.getItemInHand(InteractionHand.OFF_HAND);
        piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        boolean barterItem;
        if (piglin.isAdult()) {
            barterItem = isBarterItem(offHandItem);
            if (doBarter && barterItem) {
                if (willDropLoot) {
                    PiglinTasksHelper.dropItemsAccountingForNearbyPlayer(piglin, PiglinTasksHelper.isCheapBarterItem(offHandItem) ? getNuggetBarteringLoot(piglin) : (PiglinTasksHelper.isExpensiveBarterItem(offHandItem) ? getBlockBarteringLoot(piglin) : getBarterResponseItems(piglin)));
                }

                ReputationHelper.updatePreviousInteractorReputation(piglin, PiglinReputationType.BARTER);
                markAsBartered(offHandItem);
                putInInventory(piglin, offHandItem);
            } else if (!barterItem) {
                if (isPiglinLoved(offHandItem)) {
                    ReputationHelper.updatePreviousInteractorReputation(piglin, PiglinReputationType.GOLD_GIFT);
                }

                boolean equippedItem = piglin.equipItemIfPossible(offHandItem);
                if (!equippedItem) {
                    putInInventory(piglin, offHandItem);
                }
            }
        } else {
            barterItem = piglin.equipItemIfPossible(offHandItem);
            if (!barterItem) {
                ItemStack mainHandItem = piglin.getMainHandItem();
                if (isPiglinLoved(mainHandItem)) {
                    putInInventory(piglin, mainHandItem);
                } else {
                    PiglinTasksHelper.dropItemsAccountingForNearbyPlayer(piglin, Collections.singletonList(mainHandItem));
                }

                piglin.setItemSlot(EquipmentSlot.MAINHAND, offHandItem);
                piglin.setGuaranteedDrop(EquipmentSlot.MAINHAND);
                piglin.setPersistenceRequired();
            }
        }
    }

    /**
     * @author infamous
     */
    private static void markAsBartered(ItemStack offHandItem) {
        CompoundTag compoundNBT = offHandItem.getOrCreateTag();
        compoundNBT.putBoolean("Bartered", true);
    }

    /**
     * @author infamous
     */
    private static boolean willDropLootFor(Piglin piglinEntity, Entity interactorEntity) {
        return interactorEntity instanceof LivingEntity living && ReputationHelper.isAllowedToBarter(piglinEntity, living)
                || interactorEntity == null && !SapienceConfig.COMMON.REQUIRE_LIVING_FOR_BARTER.get();
    }

    /**
     * @author infamous
     */
    private static List<ItemStack> getNuggetBarteringLoot(AbstractPiglin piglinEntity) {
        if (piglinEntity.level.getServer() != null) {
            LootTable loottable = piglinEntity.level.getServer().getLootTables().get(PiglinTasksHelper.PIGLIN_BARTERING_CHEAP);
            return loottable.getRandomItems((new LootContext.Builder((ServerLevel) piglinEntity.level)).withParameter(LootContextParams.THIS_ENTITY, piglinEntity).withRandom(piglinEntity.level.random).create(LootContextParamSets.PIGLIN_BARTER));
        } else {
            return Collections.emptyList();
        }
    }
    /**
     * @author infamous
     */
    private static List<ItemStack> getBlockBarteringLoot(AbstractPiglin piglinEntity) {
        if (piglinEntity.level.getServer() != null) {
            LootTable loottable = piglinEntity.level.getServer().getLootTables().get(PiglinTasksHelper.PIGLIN_BARTERING_EXPENSIVE);
            return loottable.getRandomItems((new LootContext.Builder((ServerLevel)piglinEntity.level)).withParameter(LootContextParams.THIS_ENTITY, piglinEntity).withRandom(piglinEntity.level.random).create(LootContextParamSets.PIGLIN_BARTER));
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * @author infamous
     */
    private static List<ItemStack> getBarterResponseItems(AbstractPiglin piglin) {
        LootTable loottable = piglin.level.getServer().getLootTables().get(PPLootTables.ALCHEMIST_BARTER);
        return loottable.getRandomItems((new LootContext.Builder((ServerLevel)piglin.level)).withParameter(LootContextParams.THIS_ENTITY, piglin).withRandom(piglin.level.random).create(LootContextParamSets.PIGLIN_BARTER));
    }

    private static void putInInventory(Piglin piglin, ItemStack stack) {
        ItemStack remainder;
        if (isBarterItem(stack)) {
            CompoundTag tag = stack.getOrCreateTag();
            remainder = GreedHelper.addGreedItemToGreedInventory(piglin, stack, tag.getBoolean("Bartered"));
        } else {
            remainder = piglin.getInventory().addItem(stack);
        }

        if (remainder.isEmpty() || !isPiglinLoved(remainder) && !isBarterItem(remainder)) {
            dropItemsNearSelf(piglin, Collections.singletonList(remainder));
        }
    }

    private static void dropItems(AbstractPiglin piglinEntity, List<ItemStack> itemStacks, Vec3 vector3d) {
        if (!itemStacks.isEmpty()) {
            piglinEntity.swing(InteractionHand.OFF_HAND);

            for(ItemStack itemstack : itemStacks) {
                BehaviorUtils.throwItem(piglinEntity, itemstack, vector3d.add(0.0D, 1.0D, 0.0D));
            }
        }
    }

    private static void dropItemsNearSelf(AbstractPiglin piglinEntity, List<ItemStack> itemStacks) {
        dropItems(piglinEntity, itemStacks, getNearbyVectorOrPositionVector(piglinEntity));
    }

    private static Vec3 getNearbyVectorOrPositionVector(AbstractPiglin piglinEntity) {
        Vec3 vector3d = LandRandomPos.getPos(piglinEntity, 4, 2);
        return vector3d == null ? piglinEntity.position() : vector3d;
    }
}
