package tallestred.piglinproliferation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import tallestred.piglinproliferation.capablities.PPDataAttachments;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.advancement.PPCriteriaTriggers;
import tallestred.piglinproliferation.common.attribute.PPAttributes;
import tallestred.piglinproliferation.common.blockentities.FireRingBlockEntity;
import tallestred.piglinproliferation.common.blockentities.PPBlockEntities;
import tallestred.piglinproliferation.common.blocks.PPBlocks;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;
import tallestred.piglinproliferation.common.entities.PiglinTraveler;
import tallestred.piglinproliferation.common.entities.ZombifiedPiglinVariant;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.items.component.PPComponents;
import tallestred.piglinproliferation.common.loot.PPLoot;
import tallestred.piglinproliferation.common.recipes.PPRecipeSerializers;
import tallestred.piglinproliferation.common.worldgen.PPWorldgen;
import tallestred.piglinproliferation.configuration.PPConfig;
import tallestred.piglinproliferation.networking.CriticalCapabilityPacket;
import tallestred.piglinproliferation.networking.ZiglinCapabilitySyncPacket;

import static tallestred.piglinproliferation.util.CodeUtilities.potionContents;
import static tallestred.piglinproliferation.util.RegistryUtilities.addToCreativeTabAfter;

@Mod(PiglinProliferation.MODID)
public class PiglinProliferation {
    public static final String MODID = "piglinproliferation";
    public static final DataMapType<EntityType<?>, ZombifiedPiglinVariant> ZOMBIFIED_PIGLIN_VARIANT_DATA_MAP = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(MODID, "zombified_piglin_variants"), Registries.ENTITY_TYPE, ZombifiedPiglinVariant.CODEC).synced(ZombifiedPiglinVariant.WEIGHT_CODEC, false).build();

    public PiglinProliferation(IEventBus modEventBus, Dist dist, ModContainer container) {
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::processIMC);
        modEventBus.addListener(this::addAttributes);
        modEventBus.addListener(this::addCustomAttributes);
        modEventBus.addListener(this::addSpawn);
        modEventBus.addListener(this::addCreativeTabs);
        modEventBus.addListener(this::registerPackets);
        modEventBus.addListener(this::addDataMaps);
        NeoForge.EVENT_BUS.addListener(this::serverStart);
        PPSounds.SOUNDS.register(modEventBus);
        PPAttributes.ATTRIBUTES.register(modEventBus);
        PPComponents.COMPONENTS.register(modEventBus);
        PPEntityTypes.ENTITIES.register(modEventBus);
        PPItems.ITEMS.register(modEventBus);
        PPMemoryModules.MEMORY_MODULE_TYPE.register(modEventBus);
        PPActivities.ACTIVITIES.register(modEventBus);
        PPBlocks.BLOCKS.register(modEventBus);
        PPBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        PPWorldgen.STRUCTURE_TYPES.register(modEventBus);
        PPCriteriaTriggers.CRITERIA_TRIGGERS.register(modEventBus);
        PPLoot.GLM.register(modEventBus);
        PPLoot.LOOT_ITEM_FUNCTION_TYPES.register(modEventBus);
        PPLoot.LOOT_ITEM_CONDITION_TYPES.register(modEventBus);
        PPRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        PPDataAttachments.ATTACHMENT_TYPES.register(modEventBus);
        container.registerConfig(ModConfig.Type.COMMON, PPConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, PPConfig.CLIENT_SPEC);
    }


    private void addAttributes(final EntityAttributeCreationEvent event) {
        event.put(PPEntityTypes.PIGLIN_TRAVELER.get(), PiglinAlchemist.createAttributes().build());
        event.put(PPEntityTypes.PIGLIN_ALCHEMIST.get(), PiglinAlchemist.createAttributes().build());
    }

    private void addCustomAttributes(final EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes())
            event.add(type, PPAttributes.TURNING_SPEED);
    }

    private void addDataMaps(final RegisterDataMapTypesEvent event) {
        event.register(ZOMBIFIED_PIGLIN_VARIANT_DATA_MAP);
    }

    private void addCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (CreativeModeTabs.SPAWN_EGGS.equals(event.getTabKey())) {
            addToCreativeTabAfter(event, Items.PIGLIN_SPAWN_EGG, PPItems.PIGLIN_ALCHEMIST_SPAWN_EGG.get());
            addToCreativeTabAfter(event, Items.PIGLIN_BRUTE_SPAWN_EGG, PPItems.PIGLIN_TRAVELER_SPAWN_EGG.get());
        } else if (CreativeModeTabs.FUNCTIONAL_BLOCKS.equals(event.getTabKey())) {
            addToCreativeTabAfter(event, Items.PIGLIN_HEAD,
                    PPItems.PIGLIN_ALCHEMIST_HEAD_ITEM.get(),
                    PPItems.PIGLIN_TRAVELER_HEAD_ITEM.get(),
                    PPItems.PIGLIN_BRUTE_HEAD_ITEM.get(),
                    PPItems.ZOMBIFIED_PIGLIN_HEAD_ITEM.get()
            );
            addToCreativeTabAfter(event, Items.SOUL_CAMPFIRE,
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
        } else if (CreativeModeTabs.COMBAT.equals(event.getTabKey())) {
            addToCreativeTabAfter(event, Items.SHIELD, PPItems.BUCKLER.get());
        } else if (CreativeModeTabs.TOOLS_AND_UTILITIES.equals(event.getTabKey())) {
            addToCreativeTabAfter(event, Items.RECOVERY_COMPASS, PPItems.TRAVELERS_COMPASS.get());
        }
    }

    private void addSpawn(final RegisterSpawnPlacementsEvent event) {
        event.register(PPEntityTypes.PIGLIN_ALCHEMIST.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PiglinAlchemist::checkChemistSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(PPEntityTypes.PIGLIN_TRAVELER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PiglinTraveler::checkTravelerSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
    }

    private void setup(final FMLCommonSetupEvent event) {
        DispenseItemBehavior oldBehavior = DispenserBlock.DISPENSER_REGISTRY.get(Items.POTION);
        event.enqueueWork(() -> DispenserBlock.registerBehavior(Items.POTION, new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource blockSource, ItemStack stack) {
                ServerLevel level = blockSource.level();
                BlockPos frontPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
                if (level.getBlockEntity(frontPos) instanceof FireRingBlockEntity fireRing)
                    if (fireRing.addEffects(null, null, stack, potionContents(stack))) {
                        stack.shrink(1);
                        level.playSound(null, frontPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return new ItemStack(Items.GLASS_BOTTLE);
                    }
                return oldBehavior.dispense(blockSource, stack);
            }
        }));
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
    }

    private void processIMC(final InterModProcessEvent event) {
    }

    private void serverStart(final ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> templatePoolRegistry = event.getServer().registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = event.getServer().registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();
        PPWorldgen.addBuildingToPool(templatePoolRegistry, processorListRegistry,
                ResourceLocation.parse("minecraft:bastion/mobs/piglin"),
                "piglinproliferation:bastion/alchemist_piglin", PPConfig.COMMON.alchemistWeightInBastions.get());
    }

    private void registerPackets(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(CriticalCapabilityPacket.TYPE, CriticalCapabilityPacket.STREAM_CODEC, CriticalCapabilityPacket::handle);
        registrar.playToClient(ZiglinCapabilitySyncPacket.TYPE, ZiglinCapabilitySyncPacket.STREAM_CODEC, ZiglinCapabilitySyncPacket::handle);
    }
}
