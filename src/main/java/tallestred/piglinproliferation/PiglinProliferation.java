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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.MutableHashedLinkedMap;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.SpawnPlacementRegisterEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import tallestred.piglinproliferation.capablities.PPCapablities;
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
import tallestred.piglinproliferation.networking.AlchemistBeltSyncPacket;
import tallestred.piglinproliferation.networking.CriticalCapabilityPacket;
import tallestred.piglinproliferation.networking.ZiglinCapablitySyncPacket;

@Mod(PiglinProliferation.MODID)
public class PiglinProliferation {
    public static final String MODID = "piglinproliferation";

    public PiglinProliferation(IEventBus modEventBus, Dist dist) {
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::processIMC);
        modEventBus.addListener(this::addAttributes);
        modEventBus.addListener(this::addSpawn);
        modEventBus.addListener(this::addCreativeTabs);
        modEventBus.addListener(this::register);
        if (dist == Dist.CLIENT)
            modEventBus.addListener(this::doClientStuff);
        NeoForge.EVENT_BUS.addListener(this::serverStart);
        PPSounds.SOUNDS.register(modEventBus);
        PPItems.ITEMS.register(modEventBus);
        PPEntityTypes.ENTITIES.register(modEventBus);
        PPMemoryModules.MEMORY_MODULE_TYPE.register(modEventBus);
        PPActivities.ACTIVITIES.register(modEventBus);
        PPBlocks.BLOCKS.register(modEventBus);
        PPBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        PPEnchantments.ENCHANTMENTS.register(modEventBus);
        PPWorldgen.STRUCTURE_TYPES.register(modEventBus);
        PPLoot.GLM.register(modEventBus);
        PPLoot.LOOT_ITEM_FUNCTION_TYPES.register(modEventBus);
        PPLoot.LOOT_ITEM_CONDITION_TYPES.register(modEventBus);
        PPRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        PPCapablities.ATTACHMENT_TYPES.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PPConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, PPConfig.CLIENT_SPEC);
    }


    private void addAttributes(final EntityAttributeCreationEvent event) {
        event.put(PPEntityTypes.PIGLIN_TRAVELLER.get(), PiglinAlchemist.createAttributes().build());
        event.put(PPEntityTypes.PIGLIN_ALCHEMIST.get(), PiglinAlchemist.createAttributes().build());
    }

    private void addCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> creativeTab = event.getEntries();
        if (CreativeModeTabs.SPAWN_EGGS.equals(event.getTabKey())) {
            addToCreativeTabAfter(creativeTab, Items.PIGLIN_SPAWN_EGG, PPItems.PIGLIN_ALCHEMIST_SPAWN_EGG.get());
            addToCreativeTabAfter(creativeTab, Items.PIGLIN_BRUTE_SPAWN_EGG, PPItems.PIGLIN_TRAVELLER_SPAWN_EGG.get());
        } else if (CreativeModeTabs.FUNCTIONAL_BLOCKS.equals(event.getTabKey())) {
            addToCreativeTabAfter(creativeTab, Items.PIGLIN_HEAD,
                    PPItems.PIGLIN_ALCHEMIST_HEAD_ITEM.get(),
                    PPItems.PIGLIN_TRAVELLER_HEAD_ITEM.get(),
                    PPItems.PIGLIN_BRUTE_HEAD_ITEM.get(),
                    PPItems.ZOMBIFIED_PIGLIN_HEAD_ITEM.get()
            );
            addToCreativeTabAfter(creativeTab, Items.SOUL_CAMPFIRE,
                    PPItems.STONE_FIRE_RING_ITEM.get(),
                    PPItems.STONE_SOUL_FIRE_RING_ITEM.get(),
                    PPItems.DEEPSLATE_FIRE_RING_ITEM.get(),
                    PPItems.DEEPSLATE_SOUL_FIRE_RING_ITEM.get(),
                    PPItems.NETHERRACK_FIRE_RING_ITEM.get(),
                    PPItems.NETHERRACK_SOUL_FIRE_RING_ITEM.get(),
                    PPItems.BLACKSTONE_FIRE_RING_ITEM.get(),
                    PPItems.BLACKSTONE_SOUL_FIRE_RING_ITEM.get(),
                    PPItems.END_STONE_FIRE_RING_ITEM.get(),
                    PPItems.END_STONE_SOUL_FIRE_RING_ITEM.get()
            );
        } else if (CreativeModeTabs.COMBAT.equals(event.getTabKey()))
            addToCreativeTabAfter(creativeTab, Items.SHIELD, PPItems.BUCKLER.get());
    }

    private void addToCreativeTabAfter(MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> creativeTab, Item after, Item... toAdd) {
        if (toAdd.length > 0) {
            creativeTab.putAfter(new ItemStack(after), new ItemStack(toAdd[0]), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            if (toAdd.length > 1)
                for (int i = 1; i < toAdd.length; i++)
                    creativeTab.putAfter(new ItemStack(toAdd[i - 1]), new ItemStack(toAdd[i]), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
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

    @OnlyIn(Dist.CLIENT)
    private void doClientStuff(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(PPItems.BUCKLER.get(), new ResourceLocation("blocking"),
                    (stack, clientWorld, livingEntity, useTime) -> {
                        boolean active = livingEntity != null && livingEntity.isUsingItem()
                                && livingEntity.getUseItem() == stack
                                || livingEntity != null && BucklerItem.isReady(stack);
                        return livingEntity != null && active ? 1.0F : 0.0F;
                    });
            ItemProperties.register(PPItems.TRAVELLERS_COMPASS.get(), new ResourceLocation("angle"), new CompassItemPropertyFunction((level, itemStack, player) -> TravellersCompassItem.getPosition(itemStack.getOrCreateTag())));
        });
    }

    private void serverStart(final ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> templatePoolRegistry = event.getServer().registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = event.getServer().registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();
        PPWorldgen.addBuildingToPool(templatePoolRegistry, processorListRegistry,
                new ResourceLocation("minecraft:bastion/mobs/piglin"),
                "piglinproliferation:bastion/alchemist_piglin", PPConfig.COMMON.alchemistWeightInBastions.get());
    }

    private void register(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar reg = event.registrar(MODID).versioned("2.0.0");
        reg.play(AlchemistBeltSyncPacket.ID, AlchemistBeltSyncPacket::new, payload -> payload.client(AlchemistBeltSyncPacket::handle));
        reg.play(CriticalCapabilityPacket.ID, CriticalCapabilityPacket::new, payload -> payload.client(CriticalCapabilityPacket::handle));
        reg.play(ZiglinCapablitySyncPacket.ID, ZiglinCapablitySyncPacket::new, payload -> payload.client(ZiglinCapablitySyncPacket::handle));
    }


}
