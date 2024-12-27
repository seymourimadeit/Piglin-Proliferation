package tallestred.piglinproliferation.client.renderers;

import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.util.Lazy;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.client.models.BucklerModel;
import tallestred.piglinproliferation.client.models.PiglinAlchemistModel;
import tallestred.piglinproliferation.client.models.PiglinHeadEntityModel;
import tallestred.piglinproliferation.client.models.PiglinTravelerModel;
import tallestred.piglinproliferation.client.particles.ColoredSmokeParticle;
import tallestred.piglinproliferation.client.particles.PPParticles;
import tallestred.piglinproliferation.client.renderers.layers.PiglinClothingRenderLayer;
import tallestred.piglinproliferation.common.blockentities.PPBlockEntities;
import tallestred.piglinproliferation.common.blocks.PiglinSkullBlock;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;

import static tallestred.piglinproliferation.util.RegistryUtilities.addLayerToRenderer;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = PiglinProliferation.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PPRenderSetupEvents {
    public static final ModelLayerLocation ZIGLIN_CLOTHING = new ModelLayerLocation(
            ResourceLocation.parse(PiglinProliferation.MODID + "ziglin_clothing"), "ziglin_clothing");
    public static final ModelLayerLocation PIGLIN_SKULL = new ModelLayerLocation(
            ResourceLocation.parse(PiglinProliferation.MODID + "piglin_skull"), "piglin_skull");
    public static final ModelLayerLocation PIGLIN_ALCHEMIST_SKULL = new ModelLayerLocation(
            ResourceLocation.parse(PiglinProliferation.MODID + "piglin_alchemist_skull"), "piglin_alchemist_skull");
    public static final ModelLayerLocation PIGLIN_TRAVELER_SKULL = new ModelLayerLocation(
            ResourceLocation.parse(PiglinProliferation.MODID + "piglin_traveler_skull"), "piglin_traveler_skull");
    public static final ModelLayerLocation PIGLIN_ALCHEMIST = new ModelLayerLocation(
            ResourceLocation.parse(PiglinProliferation.MODID + "piglin_alchemist"), "piglin_alchemist");
    public static final ModelLayerLocation PIGLIN_TRAVELER = new ModelLayerLocation(
            ResourceLocation.parse(PiglinProliferation.MODID + "piglin_traveler"), "piglin_traveler");
    public static final ModelLayerLocation PIGLIN_ALCHEMIST_BELT_SLOTS = new ModelLayerLocation(
            ResourceLocation.parse(PiglinProliferation.MODID + "piglin_alchemist_belt"), "piglin_alchemist_belt");
    public static final ModelLayerLocation BUCKLER = new ModelLayerLocation(ResourceLocation.parse(PiglinProliferation.MODID + "buckler"),
            "buckler");
    public static final ModelLayerLocation TRAVELER_ARMOR_OUTER_LAYER = new ModelLayerLocation(
            ResourceLocation.parse(PiglinProliferation.MODID + "traveler_outer_armor"), "traveler_outer_armor");

    public static final ModelLayerLocation ALCHEMIST_ARMOR_OUTER_LAYER = new ModelLayerLocation(
            ResourceLocation.parse(PiglinProliferation.MODID + "alchemist_outer_armor"), "alchemist_outer_armor");
    @SuppressWarnings("deprecation") //It is necessary :(
    public static final Material BUCKLER_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(PiglinProliferation.MODID, "entity/buckler/golden_buckler"));

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PIGLIN_SKULL, Lazy.of(PiglinHeadEntityModel::createMesh));
        event.registerLayerDefinition(PIGLIN_ALCHEMIST_SKULL, Lazy.of(PiglinHeadEntityModel::createAlchemistMesh));
        event.registerLayerDefinition(PIGLIN_TRAVELER_SKULL, Lazy.of(PiglinHeadEntityModel::createTravelerMesh));
        event.registerLayerDefinition(ZIGLIN_CLOTHING, () -> LayerDefinition.create(PiglinModel.createMesh(new CubeDeformation(0.25F)), 64, 64));
        event.registerLayerDefinition(PIGLIN_ALCHEMIST, () -> LayerDefinition.create(PiglinAlchemistModel.createBodyLayer(new CubeDeformation(0.25F), new CubeDeformation(0.70F), new CubeDeformation(1.05F)), 120, 64));
        event.registerLayerDefinition(PIGLIN_TRAVELER, PiglinTravelerModel::createBodyLayer);
        event.registerLayerDefinition(TRAVELER_ARMOR_OUTER_LAYER, () -> LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(1.3F)), 64, 32));
        event.registerLayerDefinition(ALCHEMIST_ARMOR_OUTER_LAYER, () -> LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(1.3F)), 64, 32));
        event.registerLayerDefinition(PIGLIN_ALCHEMIST_BELT_SLOTS, () -> LayerDefinition.create(PiglinAlchemistModel.createBodyLayer(new CubeDeformation(0.40F), new CubeDeformation(1.0F), new CubeDeformation(1.40F)), 120, 64));
        event.registerLayerDefinition(BUCKLER, BucklerModel::createLayer);
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(PPBlockEntities.PIGLIN_SKULL.get(), SkullBlockRenderer::new);
        event.registerBlockEntityRenderer(PPBlockEntities.FIRE_RING.get(), FireRingRenderer::new);
        event.registerEntityRenderer(PPEntityTypes.PIGLIN_ALCHEMIST.get(), PiglinAlchemistRenderer::new);
        event.registerEntityRenderer(PPEntityTypes.PIGLIN_TRAVELER.get(), PiglinTravelerRenderer::new);
    }

    @SubscribeEvent
    public static void layer(EntityRenderersEvent.AddLayers event) {
        addLayerToRenderer(event, EntityType.ZOMBIFIED_PIGLIN, PiglinClothingRenderLayer::new);
    }

    @SubscribeEvent
    public static void registerSkullModels(EntityRenderersEvent.CreateSkullModels event) {
        event.registerSkullModel(PiglinSkullBlock.Types.PIGLIN_BRUTE, new PiglinHeadEntityModel(event.getEntityModelSet().bakeLayer(PIGLIN_SKULL)));
        event.registerSkullModel(PiglinSkullBlock.Types.ZOMBIFIED_PIGLIN, new PiglinHeadEntityModel(event.getEntityModelSet().bakeLayer(PIGLIN_SKULL)));
        event.registerSkullModel(PiglinSkullBlock.Types.PIGLIN_ALCHEMIST, new PiglinHeadEntityModel(event.getEntityModelSet().bakeLayer(PIGLIN_ALCHEMIST_SKULL)));
        event.registerSkullModel(PiglinSkullBlock.Types.PIGLIN_TRAVELER, new PiglinHeadEntityModel(event.getEntityModelSet().bakeLayer(PIGLIN_TRAVELER_SKULL)));
    }

    @SubscribeEvent
    public static void clientSetupEvent(FMLClientSetupEvent event) {
        event.enqueueWork(() -> SkullBlockRenderer.SKIN_BY_TYPE.put(PiglinSkullBlock.Types.PIGLIN_BRUTE, ResourceLocation.parse("textures/entity/piglin/piglin_brute.png")));
        event.enqueueWork(() -> SkullBlockRenderer.SKIN_BY_TYPE.put(PiglinSkullBlock.Types.ZOMBIFIED_PIGLIN, ResourceLocation.parse("textures/entity/piglin/zombified_piglin.png")));
        event.enqueueWork(() -> SkullBlockRenderer.SKIN_BY_TYPE.put(PiglinSkullBlock.Types.PIGLIN_ALCHEMIST, ResourceLocation.fromNamespaceAndPath(PiglinProliferation.MODID, "textures/entity/piglin/alchemist/alchemist.png")));
        event.enqueueWork(() -> SkullBlockRenderer.SKIN_BY_TYPE.put(PiglinSkullBlock.Types.PIGLIN_TRAVELER, ResourceLocation.fromNamespaceAndPath(PiglinProliferation.MODID, "textures/entity/piglin/traveler/traveler.png")));
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(PPParticles.COLORED_SMOKE.get(), ColoredSmokeParticle.ColoredSmokeParticleProvider::new);
        event.registerSpriteSet(PPParticles.SIGNAL_COLORED_SMOKE.get(), ColoredSmokeParticle.SignalColoredSmokeParticleProvider::new);
    }
}
