package tallestred.piglinproliferation.common.loot_tables;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.loot_tables.loot_conditions.TravellersCompassValidateCondition;
import tallestred.piglinproliferation.common.loot_tables.loot_functions.TravellersCompassLocateFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPLootTables {
    public static final BiMap<ResourceLocation, LootContextParamSet> REGISTRY = HashBiMap.create();
    public static final DeferredRegister<LootItemFunctionType> LOOT_ITEM_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, PiglinProliferation.MODID);
    public static final DeferredRegister<LootItemConditionType> LOOT_ITEM_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, PiglinProliferation.MODID);
    public static final LootContextParamSet MODDED_BARTERING = register("modded_bartering", (p_81436_) -> {
        p_81436_.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN);
    });
    public static final ResourceLocation ALCHEMIST_BARTER = new ResourceLocation(PiglinProliferation.MODID,
            "gameplay/alchemist_bartering");
    public static final ResourceLocation TRAVELLER_BARTER = new ResourceLocation(PiglinProliferation.MODID,
            "gameplay/traveller_bartering");
    public static final ResourceLocation PIGLIN_BARTERING_CHEAP = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/alchemist_bartering_cheap");
    public static final ResourceLocation PIGLIN_BARTERING_EXPENSIVE = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/alchemist_bartering_expensive");
    public static final RegistryObject<LootItemFunctionType> TRAVELLERS_COMPASS_LOCATION = LOOT_ITEM_FUNCTION_TYPES.register("travellers_compass_locate", () ->  new LootItemFunctionType(TravellersCompassLocateFunction.CODEC));
    public static final RegistryObject<LootItemConditionType> TRAVELLERS_COMPASS_VALIDATE = LOOT_ITEM_CONDITION_TYPES.register("travellers_compass_validate", () -> new LootItemConditionType(TravellersCompassValidateCondition.CODEC));
    private static final List<CompassLocateObject> TRAVELLERS_COMPASS_SEARCH_LIST = Collections.synchronizedList(new ArrayList<>());
    public static LootContextParamSet register(String p_81429_, Consumer<LootContextParamSet.Builder> p_81430_) {
        LootContextParamSet.Builder lootcontextparamset$builder = new LootContextParamSet.Builder();
        p_81430_.accept(lootcontextparamset$builder);
        LootContextParamSet lootcontextparamset = lootcontextparamset$builder.build();
        ResourceLocation resourcelocation = new ResourceLocation(PiglinProliferation.MODID + p_81429_);
        LootContextParamSet lootcontextparamset1 = REGISTRY.put(resourcelocation, lootcontextparamset);
        if (lootcontextparamset1 != null) {
            throw new IllegalStateException("Loot table parameter set " + resourcelocation + " is already registered");
        } else {
            return lootcontextparamset;
        }
    }

    public static List<CompassLocateObject> getTravellersCompassSearchList(ServerLevel level) {
        if (TRAVELLERS_COMPASS_SEARCH_LIST.isEmpty()) {
            if (level.dimension() == ServerLevel.NETHER) {
                Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
                Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
                List<Biome> possibleBiomes = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream().map(Holder::get).toList();
                List<Structure> possibleStructures = new ArrayList<>();
                for (Holder<StructureSet> holder: level.getChunkSource().getGeneratorState().possibleStructureSets())
                    holder.get().structures().forEach(entry -> possibleStructures.add(entry.structure().get()));
                TRAVELLERS_COMPASS_SEARCH_LIST.addAll(biomeRegistry.stream().filter(possibleBiomes::contains).map(b -> new CompassLocateObject(true, biomeRegistry.getKey(b))).toList());
                TRAVELLERS_COMPASS_SEARCH_LIST.addAll(structureRegistry.stream().filter(possibleStructures::contains).map(s -> new CompassLocateObject(false, structureRegistry.getKey(s))).toList());
            }
        }
        return TRAVELLERS_COMPASS_SEARCH_LIST;
    }
}
