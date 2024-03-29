package tallestred.piglinproliferation.common.loot;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import tallestred.piglinproliferation.PiglinProliferation;

import static tallestred.piglinproliferation.PiglinProliferation.MODID;

public class PPLoot {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    public static final DeferredRegister<LootItemFunctionType> LOOT_ITEM_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, PiglinProliferation.MODID);
    public static final DeferredRegister<LootItemConditionType> LOOT_ITEM_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, PiglinProliferation.MODID);

    public static final ResourceLocation ALCHEMIST_BARTER = new ResourceLocation(PiglinProliferation.MODID, "gameplay/alchemist_bartering");
    public static final ResourceLocation ALCHEMIST_BARTER_CHEAP = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/alchemist_bartering_cheap");
    public static final ResourceLocation ALCHEMIST_BARTER_EXPENSIVE = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/alchemist_bartering_expensive");

    public static final ResourceLocation TRAVELLER_BARTER = new ResourceLocation(PiglinProliferation.MODID, "gameplay/traveller_bartering");
    public static final ResourceLocation TRAVELLER_BARTER_CHEAP = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/traveller_bartering_cheap");
    public static final ResourceLocation TRAVELLER_BARTER_EXPENSIVE = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/traveller_bartering_expensive");

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> COMPASS_CAN_FIND_LOCATION = LOOT_ITEM_CONDITION_TYPES.register("compass_can_find_location", () -> new LootItemConditionType(CompassCanFindLocationCondition.CODEC));
    public static final DeferredHolder<LootItemFunctionType, LootItemFunctionType> ADD_LOCATION_TO_COMPASS = LOOT_ITEM_FUNCTION_TYPES.register("add_location_to_compass", () ->  new LootItemFunctionType(AddLocationToCompassFunction.CODEC));
    public static final DeferredHolder<Codec<? extends IGlobalLootModifier>, Codec<BastionLootModifier>> DUNGEON_LOOT = GLM.register("bastion_loot", BastionLootModifier.CODEC);
}
