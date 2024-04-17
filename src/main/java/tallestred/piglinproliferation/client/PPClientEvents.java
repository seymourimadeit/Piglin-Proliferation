package tallestred.piglinproliferation.client;

import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.util.Lazy;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.client.models.BucklerModel;
import tallestred.piglinproliferation.client.models.PiglinAlchemistModel;
import tallestred.piglinproliferation.client.models.PiglinHeadEntityModel;
import tallestred.piglinproliferation.client.models.PiglinTravellerModel;
import tallestred.piglinproliferation.client.renderers.FireRingRenderer;
import tallestred.piglinproliferation.client.renderers.PiglinAlchemistRenderer;
import tallestred.piglinproliferation.client.renderers.PiglinTravellerRenderer;
import tallestred.piglinproliferation.client.renderers.layers.PiglinClothingRenderLayer;
import tallestred.piglinproliferation.common.blockentities.PPBlockEntities;
import tallestred.piglinproliferation.common.blocks.PiglinSkullBlock;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;

import java.util.function.Function;

import static tallestred.piglinproliferation.util.RegistryUtilities.addLayerToRenderer;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PPClientEvents {
    public static final ModelLayerLocation ZIGLIN_CLOTHING = new ModelLayerLocation(
            new ResourceLocation(PiglinProliferation.MODID + "ziglin_clothing"), "ziglin_clothing");
    public static final ModelLayerLocation PIGLIN_SKULL = new ModelLayerLocation(
            new ResourceLocation(PiglinProliferation.MODID + "piglin_skull"), "piglin_skull");
    public static final ModelLayerLocation PIGLIN_ALCHEMIST_SKULL = new ModelLayerLocation(
            new ResourceLocation(PiglinProliferation.MODID + "piglin_alchemist_skull"), "piglin_alchemist_skull");
    public static final ModelLayerLocation PIGLIN_TRAVELLER_SKULL = new ModelLayerLocation(
            new ResourceLocation(PiglinProliferation.MODID + "piglin_traveller_skull"), "piglin_traveller_skull");
    public static final ModelLayerLocation PIGLIN_ALCHEMIST = new ModelLayerLocation(
            new ResourceLocation(PiglinProliferation.MODID + "piglin_alchemist"), "piglin_alchemist");
    public static final ModelLayerLocation PIGLIN_TRAVELLER = new ModelLayerLocation(
            new ResourceLocation(PiglinProliferation.MODID + "piglin_traveller"), "piglin_traveller");
    public static final ModelLayerLocation PIGLIN_ALCHEMIST_BELT_SLOTS = new ModelLayerLocation(
            new ResourceLocation(PiglinProliferation.MODID + "piglin_alchemist_belt"), "piglin_alchemist_belt");
    public static final ModelLayerLocation BUCKLER = new ModelLayerLocation(new ResourceLocation(PiglinProliferation.MODID + "buckler"),
            "buckler");
    public static final ModelLayerLocation TRAVELLER_ARMOR_OUTER_LAYER = new ModelLayerLocation(
            new ResourceLocation(PiglinProliferation.MODID + "traveller_outer_armor"), "traveller_outer_armor");

    public static final ModelLayerLocation ALCHEMIST_ARMOR_OUTER_LAYER = new ModelLayerLocation(
            new ResourceLocation(PiglinProliferation.MODID + "alchemist_outer_armor"), "alchemist_outer_armor");
    @SuppressWarnings("deprecation") //It is necessary :(
    public static final Material BUCKLER_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(PiglinProliferation.MODID, "entity/buckler/golden_buckler"));

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PIGLIN_SKULL, Lazy.of(PiglinHeadEntityModel::createMesh));
        event.registerLayerDefinition(PIGLIN_ALCHEMIST_SKULL, Lazy.of(PiglinHeadEntityModel::createAlchemistMesh));
        event.registerLayerDefinition(PIGLIN_TRAVELLER_SKULL, Lazy.of(PiglinHeadEntityModel::createTravellerMesh));
        event.registerLayerDefinition(ZIGLIN_CLOTHING, () -> LayerDefinition.create(PiglinModel.createMesh(new CubeDeformation(0.25F)), 64, 64));
        event.registerLayerDefinition(PIGLIN_ALCHEMIST, () -> LayerDefinition.create(PiglinAlchemistModel.createBodyLayer(new CubeDeformation(0.25F), new CubeDeformation(0.70F), new CubeDeformation(1.05F)), 120, 64));
        event.registerLayerDefinition(PIGLIN_TRAVELLER, PiglinTravellerModel::createBodyLayer);
        event.registerLayerDefinition(TRAVELLER_ARMOR_OUTER_LAYER, () -> LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(1.3F)), 64, 32));
        event.registerLayerDefinition(ALCHEMIST_ARMOR_OUTER_LAYER, () -> LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(1.3F)), 64, 32));
        event.registerLayerDefinition(PIGLIN_ALCHEMIST_BELT_SLOTS, () -> LayerDefinition.create(PiglinAlchemistModel.createBodyLayer(new CubeDeformation(0.40F), new CubeDeformation(1.0F), new CubeDeformation(1.40F)), 120, 64));
        event.registerLayerDefinition(BUCKLER, BucklerModel::createLayer);
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(PPBlockEntities.PIGLIN_SKULL.get(), SkullBlockRenderer::new);
        event.registerBlockEntityRenderer(PPBlockEntities.FIRE_RING.get(), FireRingRenderer::new);
        event.registerEntityRenderer(PPEntityTypes.PIGLIN_ALCHEMIST.get(), PiglinAlchemistRenderer::new);
        event.registerEntityRenderer(PPEntityTypes.PIGLIN_TRAVELLER.get(), PiglinTravellerRenderer::new);
    }

    @SubscribeEvent
    public static void layer(EntityRenderersEvent.AddLayers event) {
        addLayerToRenderer(event, EntityType.ZOMBIFIED_PIGLIN, PiglinClothingRenderLayer::new);
    }

    @SubscribeEvent
    public static void berRenderers(EntityRenderersEvent.CreateSkullModels event) {
        event.registerSkullModel(PiglinSkullBlock.Types.PIGLIN_BRUTE, new PiglinHeadEntityModel(event.getEntityModelSet().bakeLayer(PPClientEvents.PIGLIN_SKULL)));
        event.registerSkullModel(PiglinSkullBlock.Types.ZOMBIFIED_PIGLIN, new PiglinHeadEntityModel(event.getEntityModelSet().bakeLayer(PPClientEvents.PIGLIN_SKULL)));
        event.registerSkullModel(PiglinSkullBlock.Types.PIGLIN_ALCHEMIST, new PiglinHeadEntityModel(event.getEntityModelSet().bakeLayer(PPClientEvents.PIGLIN_ALCHEMIST_SKULL)));
        event.registerSkullModel(PiglinSkullBlock.Types.PIGLIN_TRAVELLER, new PiglinHeadEntityModel(event.getEntityModelSet().bakeLayer(PPClientEvents.PIGLIN_TRAVELLER_SKULL)));
    }

    @SubscribeEvent
    public static void clientSetupEvent(FMLClientSetupEvent event) {
        event.enqueueWork(() -> SkullBlockRenderer.SKIN_BY_TYPE.put(PiglinSkullBlock.Types.PIGLIN_BRUTE, new ResourceLocation("textures/entity/piglin/piglin_brute.png")));
        event.enqueueWork(() -> SkullBlockRenderer.SKIN_BY_TYPE.put(PiglinSkullBlock.Types.ZOMBIFIED_PIGLIN, new ResourceLocation("textures/entity/piglin/zombified_piglin.png")));
        event.enqueueWork(() -> SkullBlockRenderer.SKIN_BY_TYPE.put(PiglinSkullBlock.Types.PIGLIN_ALCHEMIST, new ResourceLocation(PiglinProliferation.MODID, "textures/entity/piglin/alchemist/alchemist.png")));
        event.enqueueWork(() -> SkullBlockRenderer.SKIN_BY_TYPE.put(PiglinSkullBlock.Types.PIGLIN_TRAVELLER, new ResourceLocation(PiglinProliferation.MODID, "textures/entity/piglin/traveller/traveller.png")));
    }
}
