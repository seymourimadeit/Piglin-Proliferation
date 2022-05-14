package tallestred.piglinproliferation.client.renderers.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

public class BeltRenderLayer<T extends PiglinAlchemist, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {

    public BeltRenderLayer(RenderLayerParent p_117183_) {
        super(p_117183_);
    }

    @Override
    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        SimpleContainer inventory = pLivingEntity.getPotionInventory(); //TODO have the alchemist's potion inventory sync with the client so the belt can render properly
        ItemStack itemstack = inventory.getItem(0);
        if (!itemstack.isEmpty()) {
            pMatrixStack.pushPose();
            if (this.getParentModel().young) {
                float f = 0.5F;
                pMatrixStack.translate(0.0D, 0.75D, 0.0D);
                pMatrixStack.scale(0.5F, 0.5F, 0.5F);
            }

            //   this.renderArmWithItem(pLivingEntity, itemstack1, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, pMatrixStack, pBuffer, pPackedLight);
            this.renderArmWithItem(pLivingEntity, itemstack, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, pMatrixStack, pBuffer, pPackedLight);
            pMatrixStack.popPose();
        }
    }

    protected void renderArmWithItem(LivingEntity entity, ItemStack stack, ItemTransforms.TransformType transformType, HumanoidArm arm, PoseStack poseStack, MultiBufferSource source, int light) {
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            this.getParentModel().translateToHand(arm, poseStack);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            boolean flag = arm == HumanoidArm.LEFT;
            poseStack.translate((double)((float)(flag ? -1 : 1) / 16.0F), 0.125D, -0.625D);
            Minecraft.getInstance().getItemInHandRenderer().renderItem(entity, stack, transformType, flag, poseStack, source, light);
            poseStack.popPose();
        }
    }
}
