package tallestred.piglinproliferation.common.loot_tables;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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
}
