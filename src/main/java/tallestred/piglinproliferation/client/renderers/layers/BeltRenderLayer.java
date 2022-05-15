package tallestred.piglinproliferation.client.renderers.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

public class BeltRenderLayer<T extends PiglinAlchemist, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {

    public BeltRenderLayer(RenderLayerParent p_117183_) {
        super(p_117183_);
    }

    @Override
    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        for (int inventorySlots = 0; inventorySlots < pLivingEntity.beltInventory.size(); inventorySlots++) {
            ItemStack itemstack = pLivingEntity.beltInventory.get(inventorySlots);
            if (!itemstack.isEmpty()) {
                pMatrixStack.pushPose();
                if (this.getParentModel().young) {
                    pMatrixStack.translate(0.0D, 0.75D, 0.0D);
                    pMatrixStack.scale(0.5F, 0.5F, 0.5F);
                }
                this.renderBeltItems(pLivingEntity, itemstack, ItemTransforms.TransformType.GROUND, pMatrixStack, pBuffer, pPackedLight, inventorySlots);
                pMatrixStack.popPose();
            }
        }
    }

    protected void renderBeltItems(LivingEntity entity, ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource source, int light, int inventorySlot) {
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            ((PiglinModel) this.getParentModel()).body.translateAndRotate(poseStack);
            poseStack.scale(0.8F, 0.8F, 0.8F);
            if (inventorySlot > 2 || inventorySlot < 4 && inventorySlot != 5) {
                double d = inventorySlot > 2 && inventorySlot != 5 ? 1.4 + inventorySlot * -0.4: -0.2 + inventorySlot * 0.4;
                poseStack.translate(d, 0.8D, (inventorySlot > 2 && inventorySlot != 5)  ? 0.175D: -0.175D);
            }
            if (inventorySlot == 2 || inventorySlot == 5) {
                  /*  if (inventorySlot == 2) {
                        ((PiglinModel) this.getParentModel()).rightLeg.translateAndRotate(poseStack);
                    }
                    else
                        ((PiglinModel) this.getParentModel()).leftLeg.translateAndRotate(poseStack);*/
                poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
                poseStack.translate(-0.2D, 0.4D, inventorySlot == 2 ? -0.3D : -2.1D);
            }
            poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
            Minecraft.getInstance().getItemRenderer().renderStatic(entity, stack, transformType, false, poseStack, source, entity.level, light, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), entity.getId());
            poseStack.popPose();
        }
    }
}
