package tallestred.piglinproliferation.client.renderers;

import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import tallestred.piglinproliferation.client.renderers.layers.BeltRenderLayer;

public class PiglinAlchemistRenderer extends PiglinRenderer {
    public PiglinAlchemistRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.PIGLIN, ModelLayers.PIGLIN_INNER_ARMOR, ModelLayers.PIGLIN_OUTER_ARMOR, false);
        this.addLayer(new BeltRenderLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Mob pEntity) {
        return new ResourceLocation("textures/entity/piglin/piglin.png");
    }
}
