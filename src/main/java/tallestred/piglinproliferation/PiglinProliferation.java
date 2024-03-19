package tallestred.piglinproliferation;

import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.MutableHashedLinkedMap;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.enchantments.PPEnchantments;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;
import tallestred.piglinproliferation.common.items.BucklerItem;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.blockentities.PPBlockEntities;
import tallestred.piglinproliferation.common.blocks.PPBlocks;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;
import tallestred.piglinproliferation.common.items.TravellersCompassItem;
import tallestred.piglinproliferation.common.loot.PPLoot;
import tallestred.piglinproliferation.common.recipes.PPRecipeSerializers;
import tallestred.piglinproliferation.common.worldgen.PPWorldgen;
import tallestred.piglinproliferation.configuration.PPConfig;
import tallestred.piglinproliferation.networking.PPNetworking;

@Mod(PiglinProliferation.MODID)
public class PiglinProliferation {
    public static final String MODID = "piglinproliferation";

    public PiglinProliferation() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        bus.addListener(this::enqueueIMC);
        bus.addListener(this::processIMC);
        bus.addListener(this::addAttributes);
        bus.addListener(this::addSpawn);
        bus.addListener(this::addCreativeTabs);
        bus.addListener(this::doClientStuff);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::serverStart);
        PPSounds.SOUNDS.register(bus);
        PPItems.ITEMS.register(bus);
        PPEntityTypes.ENTITIES.register(bus);
        PPMemoryModules.MEMORY_MODULE_TYPE.register(bus);
        PPActivities.ACTIVITIES.register(bus);
        PPBlockEntities.BLOCK_ENTITIES.register(bus);
        PPBlocks.BLOCKS.register(bus);
        PPEnchantments.ENCHANTMENTS.register(bus);
        PPWorldgen.STRUCTURE_TYPES.register(bus);
        PPLoot.GLM.register(bus);
        PPLoot.LOOT_ITEM_FUNCTION_TYPES.register(bus);
        PPLoot.LOOT_ITEM_CONDITION_TYPES.register(bus);
        PPRecipeSerializers.RECIPE_SERIALIZERS.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PPConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, PPConfig.CLIENT_SPEC);
        PPNetworking.registerPackets();
    }


    private void addAttributes(final EntityAttributeCreationEvent event) {
        event.put(PPEntityTypes.PIGLIN_TRAVELLER.get(), PiglinAlchemist.createAttributes().build());
        event.put(PPEntityTypes.PIGLIN_ALCHEMIST.get(), PiglinAlchemist.createAttributes().build());
    }

    private void addCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> creativeTab = event.getEntries();
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            addToCreativeTabAfter(Items.PIGLIN_SPAWN_EGG, PPItems.PIGLIN_ALCHEMIST_SPAWN_EGG.get(), creativeTab);
            addToCreativeTabAfter(Items.PIGLIN_BRUTE_SPAWN_EGG, PPItems.PIGLIN_TRAVELLER_SPAWN_EGG.get(), creativeTab);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            addToCreativeTabAfter(Items.PIGLIN_HEAD, PPItems.PIGLIN_ALCHEMIST_HEAD_ITEM.get(), creativeTab);
            addToCreativeTabAfter(PPItems.PIGLIN_ALCHEMIST_HEAD_ITEM.get(), PPItems.PIGLIN_TRAVELLER_HEAD_ITEM.get(), creativeTab);
            addToCreativeTabAfter(PPItems.PIGLIN_TRAVELLER_HEAD_ITEM.get(), PPItems.PIGLIN_BRUTE_HEAD_ITEM.get(), creativeTab);
            addToCreativeTabAfter(PPItems.PIGLIN_BRUTE_HEAD_ITEM.get(), PPItems.ZOMBIFIED_PIGLIN_HEAD_ITEM.get(), creativeTab);
            addToCreativeTabAfter(Items.SOUL_CAMPFIRE, PPItems.BLACKSTONE_FIRE_RING_ITEM.get(), creativeTab);
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT)
            addToCreativeTabAfter(Items.SHIELD, PPItems.BUCKLER.get(), creativeTab);
    }

    private void addToCreativeTabAfter(Item after, Item toAdd, MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> creativeTab) {
        creativeTab.putAfter(new ItemStack(after), new ItemStack(toAdd), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    private void addSpawn(final SpawnPlacementRegisterEvent event) {
        event.register(PPEntityTypes.PIGLIN_ALCHEMIST.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PiglinAlchemist::checkChemistSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
        event.register(PPEntityTypes.PIGLIN_TRAVELLER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PiglinTraveller::checkTravellerSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
    }

    private void processIMC(final InterModProcessEvent event) {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ItemModelHandler());
    }

    public static class ItemModelHandler {
        public ItemModelHandler() {
            ItemProperties.register(PPItems.BUCKLER.get(), new ResourceLocation("blocking"),
                    (stack, clientWorld, livingEntity, useTime) -> {
                        boolean active = livingEntity != null && livingEntity.isUsingItem()
                                && livingEntity.getUseItem() == stack
                                || livingEntity != null && BucklerItem.isReady(stack);
                        return livingEntity != null && active ? 1.0F : 0.0F;
                    });
            ItemProperties.register(PPItems.TRAVELLERS_COMPASS.get(), new ResourceLocation("angle"), new CompassItemPropertyFunction((level, itemStack, player) -> TravellersCompassItem.getPosition(itemStack.getOrCreateTag())));
        }
    }

    private void serverStart(final ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> templatePoolRegistry = event.getServer().registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = event.getServer().registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();
        PPWorldgen.addBuildingToPool(templatePoolRegistry, processorListRegistry,
                new ResourceLocation("minecraft:bastion/mobs/piglin"),
                "piglinproliferation:bastion/alchemist_piglin", PPConfig.COMMON.alchemistWeightInBastions.get());
    }
}
