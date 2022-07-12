package tallestred.piglinproliferation;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tallestred.piglinproliferation.capablities.PPCapablities;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.PPItems;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;
import tallestred.piglinproliferation.networking.PPNetworking;

import java.util.ArrayList;
import java.util.List;

@Mod(PiglinProliferation.MODID)
public class PiglinProliferation {
    public static final String MODID = "piglinproliferation";
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registry.PROCESSOR_LIST_REGISTRY, new ResourceLocation("minecraft", "empty"));

    public PiglinProliferation() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addAttributes);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::serverStart);
        PPSounds.SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPEntityTypes.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPMemoryModules.MEMORY_MODULE_TYPE.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPActivities.ACTIVITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPNetworking.registerPackets();
    }

    /**
     * Adds the building to the targeted pool.
     * <p>
     * used from https://gist.github.com/TelepathicGrunt/4fdbc445ebcbcbeb43ac748f4b18f342
     * Note: This is an additive operation which means multiple mods can do this and they stack with each other safely.
     */
    public static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
                                         Registry<StructureProcessorList> processorListRegistry,
                                         ResourceLocation poolRL,
                                         String nbtPieceRL,
                                         int weight) {

        // Grabs the processor list we want to use along with our piece.
        // This is a requirement as using the ProcessorLists.EMPTY field will cause the game to throw errors.
        // The reason why is the empty processor list in the world's registry is not the same instance as in that field once the world is started up.
        Holder<StructureProcessorList> emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);

        // Grab the pool we want to add to
        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        // Grabs the nbt piece and creates a SinglePoolElement of it that we can add to a structure's pool.
        SinglePoolElement piece = SinglePoolElement.single(nbtPieceRL, emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);

        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's templates field public for us to see.
        // Weight is handled by how many times the entry appears in this list.
        // We do not need to worry about immutability as this field is created using Lists.newArrayList(); which makes a mutable list.
        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's rawTemplates field public for us to see.
        // This list of pairs of pieces and weights is not used by vanilla by default but another mod may need it for efficiency.
        // So lets add to this list for completeness. We need to make a copy of the array as it can be an immutable list.
        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }

    private void addAttributes(final EntityAttributeCreationEvent event) {
        event.put(PPEntityTypes.PIGLIN_ALCHEMIST.get(), PiglinAlchemist.createAttributes().build());
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PPEntityTypes.registerSpawnRequirements();
        });
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
    }

    private void processIMC(final InterModProcessEvent event) {
    }

    private void serverStart(final ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> templatePoolRegistry = event.getServer().registryAccess().registry(Registry.TEMPLATE_POOL_REGISTRY).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = event.getServer().registryAccess().registry(Registry.PROCESSOR_LIST_REGISTRY).orElseThrow();
        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                new ResourceLocation("minecraft:bastion/mobs/piglin"),
                "piglinproliferation:bastion/alchemist_piglin", 4);
    }

}
