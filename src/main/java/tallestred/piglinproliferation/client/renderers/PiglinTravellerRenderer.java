package tallestred.piglinproliferation.client.renderers;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.client.PPClientEvents;
import tallestred.piglinproliferation.client.models.PiglinAlchemistModel;
import tallestred.piglinproliferation.client.models.PiglinTravellerModel;
import tallestred.piglinproliferation.client.renderers.layers.BeltRenderLayer;

public class PiglinTravellerRenderer extends PiglinRenderer {
    public PiglinTravellerRenderer(EntityRendererProvider.Context context) {
        super(context, PPClientEvents.PIGLIN_TRAVELLER, ModelLayers.PIGLIN_INNER_ARMOR, ModelLayers.PIGLIN_OUTER_ARMOR, false);
        this.model = new PiglinTravellerModel(context.bakeLayer(PPClientEvents.PIGLIN_TRAVELLER));
    }

    @Override
    public ResourceLocation getTextureLocation(Mob pEntity) {
        return new ResourceLocation(PiglinProliferation.MODID, "textures/entity/piglin/traveller/traveller.png");
    }
}
