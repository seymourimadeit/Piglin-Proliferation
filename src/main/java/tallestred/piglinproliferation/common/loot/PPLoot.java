package tallestred.piglinproliferation.common.loot;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;

import static tallestred.piglinproliferation.PiglinProliferation.MODID;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPLoot {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    public static final DeferredRegister<LootItemFunctionType> LOOT_ITEM_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, PiglinProliferation.MODID);
    public static final DeferredRegister<LootItemConditionType> LOOT_ITEM_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, PiglinProliferation.MODID);

    public static final ResourceLocation ALCHEMIST_BARTER = new ResourceLocation(PiglinProliferation.MODID, "gameplay/alchemist_bartering");
    public static final ResourceLocation ALCHEMIST_BARTER_CHEAP = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/alchemist_bartering_cheap");
    public static final ResourceLocation ALCHEMIST_BARTER_EXPENSIVE = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/alchemist_bartering_expensive");

    public static final ResourceLocation TRAVELLER_BARTER = new ResourceLocation(PiglinProliferation.MODID, "gameplay/traveller_bartering");
    public static final ResourceLocation TRAVELLER_BARTER_CHEAP = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/traveller_bartering_cheap");
    public static final ResourceLocation TRAVELLER_BARTER_EXPENSIVE = new ResourceLocation(PiglinProliferation.MODID, "gameplay/compat/traveller_bartering_expensive");
    public static final RegistryObject<LootItemConditionType> COMPASS_CAN_FIND_LOCATION = LOOT_ITEM_CONDITION_TYPES.register("compass_can_find_location", () -> new LootItemConditionType(new CompassCanFindLocationCondition.Serializer()));
    public static final RegistryObject<LootItemFunctionType> ADD_LOCATION_TO_COMPASS = LOOT_ITEM_FUNCTION_TYPES.register("add_location_to_compass", () ->  new LootItemFunctionType(new AddLocationToCompassFunction.Serializer()));
    public static final RegistryObject<Codec<BastionLootModifier>> DUNGEON_LOOT = GLM.register("bastion_loot", BastionLootModifier.CODEC);
}
