package tallestred.piglinproliferation;

import com.infamous.sapience.SapienceConfig;
import com.infamous.sapience.util.GreedHelper;
import com.infamous.sapience.util.PiglinReputationType;
import com.infamous.sapience.util.ReputationHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.common.loot_tables.PPLootTables;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.infamous.sapience.util.PiglinTasksHelper.*;

public class ModCompat {
    /**
     * @author infamous
     */
    public static void stopHoldingOffHandItem(Piglin piglin, boolean doBarter, ResourceLocation cheapBartering, ResourceLocation expensiveBartering) {
        Entity interactor = ReputationHelper.getPreviousInteractor(piglin);
        boolean willDropLoot = willDropLootFor(piglin, interactor);

        ItemStack offHandItem = piglin.getItemInHand(InteractionHand.OFF_HAND);
        piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        if (piglin.isAdult()) {
            boolean barterItem = isBarterItem(offHandItem);
            if (doBarter && barterItem) {
                if (willDropLoot) dropItemsAccountingForNearbyPlayer(piglin,
                        isCheapBarterItem(offHandItem) ?
                                getNuggetBarteringLoot(piglin, cheapBartering) :
                                isExpensiveBarterItem(offHandItem) ?
                                        getBlockBarteringLoot(piglin, expensiveBartering) :
                                        getBarterResponseItems(piglin));
                ReputationHelper.updatePreviousInteractorReputation(piglin, PiglinReputationType.BARTER);
                markAsBartered(offHandItem);
                putInInventory(piglin, offHandItem);
            } else if (!barterItem) {
                if (isPiglinLoved(offHandItem)) {
                    ReputationHelper.updatePreviousInteractorReputation(piglin, PiglinReputationType.GOLD_GIFT);
                }
                boolean equippedItem = !piglin.equipItemIfPossible(offHandItem).isEmpty();
                if (!equippedItem) {
                    putInInventory(piglin, offHandItem);
                }
            }
        } else {
            boolean equippedItem = !piglin.equipItemIfPossible(offHandItem).isEmpty();
            if (!equippedItem) {
                ItemStack mainHandItem = piglin.getMainHandItem();
                if (isPiglinLoved(mainHandItem)) {
                    putInInventory(piglin, mainHandItem);
                } else {
                    dropItemsAccountingForNearbyPlayer(piglin, Collections.singletonList(mainHandItem));
                }
                piglin.setItemSlot(EquipmentSlot.MAINHAND, offHandItem);
                piglin.setGuaranteedDrop(EquipmentSlot.MAINHAND);
                piglin.setPersistenceRequired();
            }
        }
    }

    public static void dropItemsAccountingForNearbyPlayer(AbstractPiglin piglinEntity, List<ItemStack> itemStacks) {
        Optional<Player> optionalPlayerEntity = getNearestVisiblePlayer(piglinEntity);
        if (optionalPlayerEntity.isPresent()) {
            dropItemsNearPlayer(piglinEntity, optionalPlayerEntity.get(), itemStacks);
        } else {
            dropItemsNearSelf(piglinEntity, itemStacks);
        }
    }

    public static boolean isExpensiveBarterItem(ItemStack item) {
        return item.is(PIGLINS_BARTER_EXPENSIVE);
    }

    public static boolean isCheapBarterItem(ItemStack item) {
        return item.is(PIGLINS_BARTER_CHEAP);
    }


    private static void dropItemsNearPlayer(AbstractPiglin piglinEntity, Player playerEntity, List<ItemStack> itemStacks) {
        dropItems(piglinEntity, itemStacks, playerEntity.position());
    }


    private static Optional<Player> getNearestVisiblePlayer(AbstractPiglin piglinEntity) {
        return piglinEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
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
    private static List<ItemStack> getNuggetBarteringLoot(AbstractPiglin piglinEntity, ResourceLocation table) {
        if (piglinEntity.level().getServer() != null) {
            LootTable loottable =  piglinEntity.level().getServer().getLootData().getLootTable(table);
            return loottable.getRandomItems((new LootParams.Builder((ServerLevel)piglinEntity.level())).withParameter(LootContextParams.THIS_ENTITY, piglinEntity).create(LootContextParamSets.PIGLIN_BARTER));
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * @author infamous
     */
    private static List<ItemStack> getBlockBarteringLoot(AbstractPiglin piglinEntity, ResourceLocation table) {
        if (piglinEntity.level().getServer() != null) {
            LootTable loottable = piglinEntity.level().getServer().getLootData().getLootTable(table);
            return loottable.getRandomItems((new LootParams.Builder((ServerLevel) piglinEntity.level())).withParameter(LootContextParams.THIS_ENTITY, piglinEntity).create(LootContextParamSets.PIGLIN_BARTER));
        }
        return Collections.emptyList();
    }

    /**
     * @author infamous
     */
    private static List<ItemStack> getBarterResponseItems(AbstractPiglin piglin) {
        LootTable loottable = piglin.level().getServer().getLootData().getLootTable(PPLootTables.ALCHEMIST_BARTER);
        return loottable.getRandomItems((new LootParams.Builder((ServerLevel) piglin.level())).withParameter(LootContextParams.THIS_ENTITY, piglin).create(LootContextParamSets.PIGLIN_BARTER));
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

            for (ItemStack itemstack : itemStacks) {
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