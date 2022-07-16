package tallestred.piglinproliferation.client.renderers.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import tallestred.piglinproliferation.PPEvents;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.capablities.TransformationSourceListener;
import tallestred.piglinproliferation.client.PPClientEvents;

public class PiglinClothingRenderLayer<T extends ZombifiedPiglin, M extends PiglinModel<T>> extends RenderLayer<T, M> {
    private final PiglinModel<T> layerModel;

    public PiglinClothingRenderLayer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
        this.layerModel = new PiglinModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(PPClientEvents.ZIGLIN_CLOTHING));
        this.layerModel.rightEar.visible = false;
    }

    @Override
    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (getClothingTexture(pLivingEntity) != null)
            coloredCutoutModelCopyLayerRender(this.getParentModel(), this.layerModel, getClothingTexture(pLivingEntity), pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, pPartialTicks, 1.0F, 1.0F, 1.0F);
    }


    protected ResourceLocation getClothingTexture(T livingEntity) { // TODO if it can't find a texture for a piglin fall back to the regular piglin texture
        TransformationSourceListener transformationSourceListener = PPEvents.getTransformationSourceListener(livingEntity);
        if (!transformationSourceListener.getTransformationSource().isEmpty())
            return new ResourceLocation(PiglinProliferation.MODID, "textures/entity/piglin/clothing_" + transformationSourceListener.getTransformationSource() + ".png");
        else
            return null;
    }
}
