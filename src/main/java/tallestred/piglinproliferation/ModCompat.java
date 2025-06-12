package tallestred.piglinproliferation;

import com.github.thedeathlycow.moregeodes.forge.entity.MoreGeodesMemoryModules;
import com.github.thedeathlycow.moregeodes.forge.item.tag.MoreGeodesItemTags;
import com.infamous.sapience.SapienceConfig;
import com.infamous.sapience.util.GreedHelper;
import com.infamous.sapience.util.PiglinReputationType;
import com.infamous.sapience.util.ReputationHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.fml.ModList;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;
import tallestred.piglinproliferation.common.entities.PiglinTraveler;
import tallestred.piglinproliferation.common.entities.ai.AbstractPiglinAi;
import tallestred.piglinproliferation.common.entities.ai.PiglinAlchemistAi;
import tallestred.piglinproliferation.common.entities.ai.PiglinTravelerAi;
import tallestred.piglinproliferation.common.loot.PPLoot;

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
        AbstractPiglinAi<Piglin> ai = getAiInstance(piglin);
        if (ModList.get().isLoaded("geodes")) {
            if (offHandItem.is(MoreGeodesItemTags.INSTANCE.getFOOLS_FOLD())) {
                piglin.getBrain().setMemory(MoreGeodesMemoryModules.INSTANCE.getREMEMBERS_FOOLS_GOLD(), true);
                offHandItem.setCount(0);
                piglin.swing(InteractionHand.OFF_HAND);
                Optional<Player> rememberedPlayer = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
                rememberedPlayer.ifPresent(player -> {
                    piglin.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, player);
                });
            }
        }
        if (piglin.isAdult()) {
            boolean barterItem = isBarterItem(offHandItem);
            if (doBarter && barterItem) {
                if (willDropLoot)ai.throwItems(piglin,
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
                    ai.throwItems(piglin, Collections.singletonList(mainHandItem));
                }
                piglin.setItemSlot(EquipmentSlot.MAINHAND, offHandItem);
                piglin.setGuaranteedDrop(EquipmentSlot.MAINHAND);
                piglin.setPersistenceRequired();
            }
        }
    }


    public static boolean isExpensiveBarterItem(ItemStack item) {
        return item.is(PIGLINS_BARTER_EXPENSIVE);
    }

    public static boolean isCheapBarterItem(ItemStack item) {
        return item.is(PIGLINS_BARTER_CHEAP);
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
        MinecraftServer server = piglinEntity.level().getServer();
        if (server != null) {
            LootTable loottable = server.getLootData().getLootTable(table);
            return loottable.getRandomItems((new LootParams.Builder((ServerLevel)piglinEntity.level())).withParameter(LootContextParams.THIS_ENTITY, piglinEntity).create(LootContextParamSets.PIGLIN_BARTER));
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * @author infamous
     */
    private static List<ItemStack> getBlockBarteringLoot(AbstractPiglin piglinEntity, ResourceLocation table) {
        MinecraftServer server = piglinEntity.level().getServer();
        if (server != null) {
            LootTable loottable = server.getLootData().getLootTable(table);
            return loottable.getRandomItems((new LootParams.Builder((ServerLevel) piglinEntity.level())).withParameter(LootContextParams.THIS_ENTITY, piglinEntity).create(LootContextParamSets.PIGLIN_BARTER));
        }
        return Collections.emptyList();
    }

    /**
     * @author infamous
     */
    private static List<ItemStack> getBarterResponseItems(AbstractPiglin piglin) {
        ResourceLocation lootTableID =
                piglin instanceof PiglinTraveler ? PPLoot.TRAVELER_BARTER :
                        piglin instanceof PiglinAlchemist ? PPLoot.ALCHEMIST_BARTER :
                                BuiltInLootTables.PIGLIN_BARTERING;
        MinecraftServer server = piglin.level().getServer();
        if (server != null)
            return server.getLootData().getLootTable(lootTableID).getRandomItems((new LootParams.Builder((ServerLevel) piglin.level())).withParameter(LootContextParams.THIS_ENTITY, piglin).create(LootContextParamSets.PIGLIN_BARTER));
        else return null;
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
            getAiInstance(piglin).throwItemsTowardRandomPos(piglin, Collections.singletonList(remainder));
        }
    }

    //This is a bit hacky, but it'll work as long as the correct arguments are passed. There may be a better solution in future
    @SuppressWarnings("unchecked")
    private static <T extends Piglin> AbstractPiglinAi<T> getAiInstance(Piglin piglin) {
        AbstractPiglinAi<? extends Piglin> ai = piglin instanceof PiglinAlchemist ? PiglinAlchemistAi.INSTANCE : piglin instanceof PiglinTraveler ? PiglinTravelerAi.INSTANCE : null;
        try {
            return (AbstractPiglinAi<T>) ai;
        } catch (ClassCastException ignored) {
            throw new IllegalArgumentException("Cannot be applied to Piglin subclasses that aren't from Piglin Proliferation!");
        }
    }
}