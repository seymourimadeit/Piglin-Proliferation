package tallestred.piglinproliferation.client.renderers.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.client.PPClientEvents;
import tallestred.piglinproliferation.client.models.PiglinAlchemistModel;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

public class BeltRenderLayer<T extends PiglinAlchemist, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {
    private final ItemInHandRenderer itemInHandRenderer;
    private final PiglinAlchemistModel<T> layerModel;

    public BeltRenderLayer(RenderLayerParent p_117183_, ItemInHandRenderer itemInHandRenderer) {
        super(p_117183_);
        this.layerModel = new PiglinAlchemistModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(PPClientEvents.PIGLIN_ALCHEMIST_BELT_SLOTS));
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
                this.renderBeltItems(pLivingEntity, itemstack, ItemDisplayContext.GROUND, pMatrixStack, pBuffer, pAgeInTicks, pPackedLight, inventorySlots);
                pMatrixStack.popPose();
            }
            HumanoidArm arm = pLivingEntity.isLeftHanded() ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
            ItemDisplayContext transformType = pLivingEntity.isLeftHanded() ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            if (pLivingEntity.isGonnaThrowPotion()) {
                pMatrixStack.pushPose();
                if (this.getParentModel().young) {
                    pMatrixStack.translate(0.0D, 0.75D, 0.0D);
                    pMatrixStack.scale(0.5F, 0.5F, 0.5F);
                }
                this.renderArmWithItem(pLivingEntity, pLivingEntity.getItemShownOnOffhand(), transformType, arm, pMatrixStack, pBuffer, pPackedLight);
                pMatrixStack.popPose();
            }
        }
        coloredCutoutModelCopyLayerRender(this.getParentModel(), layerModel, new ResourceLocation(PiglinProliferation.MODID, "textures/entity/piglin/alchemist/belt.png"), pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, pPartialTicks, 1.0F, 1.0F, 1.0F);
    }

    protected void renderBeltItems(LivingEntity entity, ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource source, float ageInTicks, int light, int inventorySlot) {
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            ((PiglinModel) this.getParentModel()).body.translateAndRotate(poseStack);
            poseStack.scale(0.8F, 0.8F, 0.8F);
            if (inventorySlot > 2 || inventorySlot != 5) {
                double d = -0.24 + inventorySlot * 0.44;
                double inflation = entity.hasItemInSlot(EquipmentSlot.LEGS) && !entity.hasItemInSlot(EquipmentSlot.CHEST) ? 0.215D : entity.hasItemInSlot(EquipmentSlot.CHEST) ? 0.225D : 0.165D;
                poseStack.translate((inventorySlot > 2 && inventorySlot != 5) ? -d + 1.25 : d, 0.8D, (inventorySlot > 2 && inventorySlot != 5) ? inflation : -inflation);
            }
            if (inventorySlot == 2 || inventorySlot == 5) {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                double secondSlotInflation = entity.hasItemInSlot(EquipmentSlot.LEGS) && !entity.hasItemInSlot(EquipmentSlot.CHEST) ? -0.290D : entity.hasItemInSlot(EquipmentSlot.CHEST) ? -0.265D : -0.325D;
                double fifthSlotInflation = entity.hasItemInSlot(EquipmentSlot.LEGS) && !entity.hasItemInSlot(EquipmentSlot.CHEST) ? -2.310D : entity.hasItemInSlot(EquipmentSlot.CHEST) ? -2.335D : -2.275D;
                poseStack.translate(-0.2D, 0.4D, inventorySlot == 2 ? secondSlotInflation : fifthSlotInflation);
            }
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, entity.level(), entity, light);
            Minecraft.getInstance().getItemRenderer().render(stack, transformType, false, poseStack, source, light, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), model);
            poseStack.popPose();
        }
    }


    protected void renderArmWithItem(LivingEntity p_117185_, ItemStack p_117186_, ItemDisplayContext p_117187_, HumanoidArm p_117188_, PoseStack p_117189_, MultiBufferSource p_117190_, int p_117191_) {
        if (!p_117186_.isEmpty()) {
            p_117189_.pushPose();
            this.getParentModel().translateToHand(p_117188_, p_117189_);
            p_117189_.mulPose(Axis.XP.rotationDegrees(-90.0F));
            p_117189_.mulPose(Axis.YP.rotationDegrees(180.0F));
            boolean flag = p_117188_ == HumanoidArm.LEFT;
            p_117189_.translate((double) ((float) (flag ? -1 : 1) / 16.0F), 0.125D, -0.625D);
            this.itemInHandRenderer.renderItem(p_117185_, p_117186_, p_117187_, flag, p_117189_, p_117190_, p_117191_);
            p_117189_.popPose();
        }
    }
}
