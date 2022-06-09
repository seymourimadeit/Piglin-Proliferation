package tallestred.piglinproliferation.client.renderers.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

public class BeltRenderLayer<T extends PiglinAlchemist, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {
    private final ItemInHandRenderer itemInHandRenderer;

    public BeltRenderLayer(RenderLayerParent p_117183_, ItemInHandRenderer itemInHandRenderer) {
        super(p_117183_);
        this.itemInHandRenderer = itemInHandRenderer;
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
            HumanoidArm arm = pLivingEntity.isLeftHanded() ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
            ItemTransforms.TransformType transformType = pLivingEntity.isLeftHanded() ? ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND : ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
            if (pLivingEntity.isGonnaThrowPotion()) {
                pMatrixStack.pushPose();
                if (this.getParentModel().young) {
                    pMatrixStack.translate(0.0D, 0.75D, 0.0D);
                    pMatrixStack.scale(0.5F, 0.5F, 0.5F);
                }
                this.renderArmWithItem(pLivingEntity, pLivingEntity.getPotionAboutToThrown(), transformType, arm, pMatrixStack, pBuffer, pPackedLight);
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
                double d = inventorySlot > 2 && inventorySlot != 5 ? 1.4 + inventorySlot * -0.4 : -0.2 + inventorySlot * 0.4;
                poseStack.translate(d, 0.8D, (inventorySlot > 2 && inventorySlot != 5) ? 0.175D : -0.175D);
            }
            if (inventorySlot == 2 || inventorySlot == 5) {
                poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
                poseStack.translate(-0.2D, 0.4D, inventorySlot == 2 ? -0.3D : -2.1D);
            }
            poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
            Minecraft.getInstance().getItemRenderer().renderStatic(entity, stack, transformType, false, poseStack, source, entity.level, light, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), entity.getId());
            poseStack.popPose();
        }
    }

    protected void renderArmWithItem(LivingEntity p_117185_, ItemStack p_117186_, ItemTransforms.TransformType p_117187_, HumanoidArm p_117188_, PoseStack p_117189_, MultiBufferSource p_117190_, int p_117191_) {
        if (!p_117186_.isEmpty()) {
            p_117189_.pushPose();
            this.getParentModel().translateToHand(p_117188_, p_117189_);
            p_117189_.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            p_117189_.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            boolean flag = p_117188_ == HumanoidArm.LEFT;
            p_117189_.translate((double) ((float) (flag ? -1 : 1) / 16.0F), 0.125D, -0.625D);
            this.itemInHandRenderer.renderItem(p_117185_, p_117186_, p_117187_, flag, p_117189_, p_117190_, p_117191_);
            p_117189_.popPose();
        }
    }
}
