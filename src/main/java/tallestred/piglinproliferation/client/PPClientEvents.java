package tallestred.piglinproliferation.client;

import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.client.renderers.PiglinAlchemistRenderer;
import tallestred.piglinproliferation.client.renderers.layers.PiglinClothingRenderLayer;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PPClientEvents {
    public static ModelLayerLocation ZIGLIN_CLOTHING = new ModelLayerLocation(
            new ResourceLocation(PiglinProliferation.MODID + "ziglin_clothing"), "ziglin_clothing");
    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ZIGLIN_CLOTHING, () -> LayerDefinition.create(PiglinModel.createMesh(new CubeDeformation(0.25F)), 64, 64));
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PPEntityTypes.PIGLIN_ALCHEMIST.get(), PiglinAlchemistRenderer::new);
    }

    @SubscribeEvent
    public static void layer(EntityRenderersEvent.AddLayers event) {
        addLayerToRenderer(event, EntityType.ZOMBIFIED_PIGLIN, PiglinClothingRenderLayer::new);
    }

    private static <T extends Mob, R extends LivingEntityRenderer<T, M>, M extends PiglinModel<T>> void addLayerToRenderer(EntityRenderersEvent.AddLayers event, EntityType<T> entityType, Function<R, ? extends RenderLayer<T, M>> factory) {
        R renderer = event.getRenderer(entityType);
        if (renderer != null) renderer.addLayer(factory.apply(renderer));
    }
}