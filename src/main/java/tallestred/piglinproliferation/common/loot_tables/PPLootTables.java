package tallestred.piglinproliferation.common.loot_tables;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.loot_tables.loot_conditions.TravellersCompassBiomeAlreadyRolledCondition;
import tallestred.piglinproliferation.common.loot_tables.loot_conditions.TravellersCompassStructureAlreadyRolledCondition;
import tallestred.piglinproliferation.common.loot_tables.loot_functions.TravellerCompassBiomeLocateFunction;
import tallestred.piglinproliferation.common.loot_tables.loot_functions.TravellerCompassStructureLocateFunction;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPLootTables {
    public static final DeferredRegister<LootItemFunctionType> LOOT_ITEM_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, PiglinProliferation.MODID);
    public static final DeferredRegister<LootItemConditionType> LOOT_ITEM_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, PiglinProliferation.MODID);

    public static final ResourceLocation ALCHEMIST_BARTER = new ResourceLocation(PiglinProliferation.MODID,
            "gameplay/alchemist_bartering");
    public static final ResourceLocation TRAVELLER_BARTER = new ResourceLocation(PiglinProliferation.MODID,
            "gameplay/traveller_bartering");
    public static final ResourceLocation PIGLIN_BARTERING_CHEAP = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/alchemist_bartering_cheap");
    public static final ResourceLocation PIGLIN_BARTERING_EXPENSIVE = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/alchemist_bartering_expensive");
    public static final RegistryObject<LootItemFunctionType> TRAVELLERS_COMPASS_LOCATION = LOOT_ITEM_FUNCTION_TYPES.register("travellers_compass_location", () ->  new LootItemFunctionType(TravellerCompassStructureLocateFunction.CODEC));
    public static final RegistryObject<LootItemFunctionType> TRAVELLERS_BIOME_COMPASS_LOCATION = LOOT_ITEM_FUNCTION_TYPES.register("travellers_compass_biome_location", () ->  new LootItemFunctionType(TravellerCompassBiomeLocateFunction.CODEC));
    public static final RegistryObject<LootItemConditionType> TRAVELLERS_COMPASS_BIOME_ALREADY_ROLLED = LOOT_ITEM_CONDITION_TYPES.register("travellers_compass_biome_already_rolled", () -> new LootItemConditionType(TravellersCompassBiomeAlreadyRolledCondition.CODEC));
    public static final RegistryObject<LootItemConditionType> TRAVELLERS_COMPASS_STRUCTURE_ALREADY_ROLLED = LOOT_ITEM_CONDITION_TYPES.register("travellers_compass_structure_already_rolled", () -> new LootItemConditionType(TravellersCompassStructureAlreadyRolledCondition.CODEC));
}
