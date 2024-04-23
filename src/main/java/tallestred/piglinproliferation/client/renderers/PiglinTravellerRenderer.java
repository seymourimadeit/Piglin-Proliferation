package tallestred.piglinproliferation.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.client.models.PiglinTravellerModel;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;

public class PiglinTravellerRenderer extends PiglinRenderer {
    public PiglinTravellerRenderer(EntityRendererProvider.Context context) {
        super(context, PPRenderSetupEvents.PIGLIN_TRAVELLER, ModelLayers.PIGLIN_INNER_ARMOR, PPRenderSetupEvents.TRAVELLER_ARMOR_OUTER_LAYER, false);
        this.model = new PiglinTravellerModel(context.bakeLayer(PPRenderSetupEvents.PIGLIN_TRAVELLER));
    }


    @Override
    protected void setupRotations(Mob pEntity, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        super.setupRotations(pEntity, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
        if (pEntity instanceof PiglinTraveller traveller && traveller.isSitting()) {
            pMatrixStack.translate(-0.0F, -0.6F, -0.0F);
        }

    }

    @Override
    public ResourceLocation getTextureLocation(Mob pEntity) {
        return new ResourceLocation(PiglinProliferation.MODID, "textures/entity/piglin/traveller/traveller.png");
    }
}
