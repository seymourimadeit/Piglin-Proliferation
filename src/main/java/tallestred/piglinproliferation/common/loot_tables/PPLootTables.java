package tallestred.piglinproliferation.common.loot_tables;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPLootTables {
    public static final DeferredRegister<LootItemFunctionType> LOOT_ITEM_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, PiglinProliferation.MODID);
    public static final ResourceLocation ALCHEMIST_BARTER = new ResourceLocation(PiglinProliferation.MODID,
            "gameplay/alchemist_bartering");
    public static final ResourceLocation TRAVELLER_BARTER = new ResourceLocation(PiglinProliferation.MODID,
            "gameplay/traveller_bartering");
    public static final ResourceLocation TRAVELLER_BARTER_CHEAP = new ResourceLocation(PiglinProliferation.MODID,
            "gameplay/traveller_bartering_cheap");
    public static final ResourceLocation TRAVELLER_BARTER_EXPENSIVE = new ResourceLocation(PiglinProliferation.MODID,
            "gameplay/traveller_bartering_expensive");
    public static final ResourceLocation PIGLIN_BARTERING_CHEAP = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/alchemist_bartering_cheap");
    public static final ResourceLocation PIGLIN_BARTERING_EXPENSIVE = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/alchemist_bartering_expensive");
    public static final RegistryObject<LootItemFunctionType> TRAVELLERS_COMPASS_LOCATION = LOOT_ITEM_FUNCTION_TYPES.register("travellers_compass_location", () ->  new LootItemFunctionType(new TravellerCompassStructureLocateFunction.Serializer()));
    public static final RegistryObject<LootItemFunctionType> TRAVELLERS_BIOME_COMPASS_LOCATION = LOOT_ITEM_FUNCTION_TYPES.register("travellers_compass_biome_location", () ->  new LootItemFunctionType(new TravellerCompassBiomeLocateFunction.Serializer()));
}
