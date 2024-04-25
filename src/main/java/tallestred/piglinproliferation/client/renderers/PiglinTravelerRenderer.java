package tallestred.piglinproliferation.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.client.models.PiglinTravelerModel;
import tallestred.piglinproliferation.common.entities.PiglinTraveler;

public class PiglinTravelerRenderer extends PiglinRenderer {
    public PiglinTravelerRenderer(EntityRendererProvider.Context context) {
        super(context, PPRenderSetupEvents.PIGLIN_TRAVELER, ModelLayers.PIGLIN_INNER_ARMOR, PPRenderSetupEvents.TRAVELER_ARMOR_OUTER_LAYER, false);
        this.model = new PiglinTravelerModel(context.bakeLayer(PPRenderSetupEvents.PIGLIN_TRAVELER));
    }

    @Override
    protected void setupRotations(Mob pEntity, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks, float amogus) {
        super.setupRotations(pEntity, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks, amogus);
        if (pEntity instanceof PiglinTraveler traveler && traveler.isSitting()) {
            pMatrixStack.translate(-0.0F, -0.6F, -0.0F);
        }

    }

    @Override
    public ResourceLocation getTextureLocation(Mob pEntity) {
        return new ResourceLocation(PiglinProliferation.MODID, "textures/entity/piglin/traveler/traveler.png");
    }
}
