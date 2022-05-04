package tallestred.piglinproliferation.client;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PPClientEvents {
    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PPEntityTypes.PIGLIN_ALCHEMIST.get(), (p_174070_) -> {
            return new PiglinRenderer(p_174070_, ModelLayers.PIGLIN, ModelLayers.PIGLIN_INNER_ARMOR, ModelLayers.PIGLIN_OUTER_ARMOR, false);
        });
    }
}
