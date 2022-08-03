package tallestred.piglinproliferation.client.renderers.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import org.jetbrains.annotations.NotNull;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.client.PPClientEvents;
import tallestred.piglinproliferation.client.renderers.models.PiglinAlchemistModel;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;
import tallestred.piglinproliferation.configuration.PPConfig;

import static net.minecraft.client.renderer.entity.ItemRenderer.*;

public class BeltRenderLayer<T extends PiglinAlchemist, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {
    private final PiglinAlchemistModel<T> layerModel;

    public BeltRenderLayer(RenderLayerParent p_117183_) {
        super(p_117183_);
        this.layerModel = new PiglinAlchemistModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(PPClientEvents.PIGLIN_ALCHEMIST_BELT));
    }

    @Override
    public void render(PoseStack pMatrixStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
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
                this.renderArmWithItem(pLivingEntity, pLivingEntity.getItemShownOnOffhand(), transformType, arm, pMatrixStack, pBuffer, pPackedLight);
                pMatrixStack.popPose();
            }
        }
        coloredCutoutModelCopyLayerRender(this.getParentModel(), this.layerModel, new ResourceLocation(PiglinProliferation.MODID, "textures/entity/piglin/alchemist/belt.png"), pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, pPartialTicks, 1.0F, 1.0F, 1.0F);
    }

    protected void renderBeltItems(LivingEntity entity, ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource source, int light, int inventorySlot) {
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
                poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
                double secondSlotInflation = entity.hasItemInSlot(EquipmentSlot.LEGS) && !entity.hasItemInSlot(EquipmentSlot.CHEST) ? -0.290D : entity.hasItemInSlot(EquipmentSlot.CHEST) ? -0.265D : -0.34D;
                double fifthSlotInflation = entity.hasItemInSlot(EquipmentSlot.LEGS) && !entity.hasItemInSlot(EquipmentSlot.CHEST) ? -2.310D : entity.hasItemInSlot(EquipmentSlot.CHEST) ? -2.335D : -2.26D;
                poseStack.translate(-0.2D, 0.4D, inventorySlot == 2 ? secondSlotInflation : fifthSlotInflation);
            }
            poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
            BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, entity.level, entity, light);
            this.renderItemsBeltNoGlow(stack, transformType, false, poseStack, source, light, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), model, Minecraft.getInstance().getItemRenderer());
            poseStack.popPose();
        }
    }

    public void renderItemsBeltNoGlow(ItemStack pItemStack, ItemTransforms.TransformType pTransformType, boolean pLeftHand, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel, ItemRenderer renderer) {
        if (!pItemStack.isEmpty()) {
            pMatrixStack.pushPose();
            boolean flag = pTransformType == ItemTransforms.TransformType.GUI || pTransformType == ItemTransforms.TransformType.GROUND || pTransformType == ItemTransforms.TransformType.FIXED;
            if (flag) {
                if (pItemStack.is(Items.TRIDENT)) {
                    pModel = renderer.getItemModelShaper().getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
                } else if (pItemStack.is(Items.SPYGLASS)) {
                    pModel = renderer.getItemModelShaper().getModelManager().getModel(new ModelResourceLocation("minecraft:spyglass#inventory"));
                }
            }

            pModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(pMatrixStack, pModel, pTransformType, pLeftHand);
            pMatrixStack.translate(-0.5D, -0.5D, -0.5D);
            if (!pModel.isCustomRenderer() && (!pItemStack.is(Items.TRIDENT) || flag)) {
                boolean flag1;
                if (pTransformType != ItemTransforms.TransformType.GUI && !pTransformType.firstPerson() && pItemStack.getItem() instanceof BlockItem) {
                    Block block = ((BlockItem)pItemStack.getItem()).getBlock();
                    flag1 = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
                } else {
                    flag1 = true;
                }
                if (pModel.isLayered()) { net.minecraftforge.client.ForgeHooksClient.drawItemLayered(renderer, pModel, pItemStack, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay, flag1); }
                else {
                    RenderType rendertype = ItemBlockRenderTypes.getRenderType(pItemStack, flag1);
                    VertexConsumer vertexconsumer;
                    if (pItemStack.is(Items.COMPASS) && pItemStack.hasFoil()) {
                        pMatrixStack.pushPose();
                        PoseStack.Pose posestack$pose = pMatrixStack.last();
                        if (pTransformType == ItemTransforms.TransformType.GUI) {
                            posestack$pose.pose().multiply(0.5F);
                        } else if (pTransformType.firstPerson()) {
                            posestack$pose.pose().multiply(0.75F);
                        }

                        if (flag1) {
                            vertexconsumer = getCompassFoilBufferDirect(pBuffer, rendertype, posestack$pose);
                        } else {
                            vertexconsumer = getCompassFoilBuffer(pBuffer, rendertype, posestack$pose);
                        }

                        pMatrixStack.popPose();
                    } else if (flag1) {
                        vertexconsumer = getFoilBufferDirect(pBuffer, rendertype, true, PPConfig.CLIENT.beltTextureGlow.get() && pItemStack.hasFoil());
                    } else {
                        vertexconsumer = getFoilBuffer(pBuffer, rendertype, true, PPConfig.CLIENT.beltTextureGlow.get() && pItemStack.hasFoil());
                    }

                    renderer.renderModelLists(pModel, pItemStack, pCombinedLight, pCombinedOverlay, pMatrixStack, vertexconsumer);
                }
            } else {
                net.minecraftforge.client.RenderProperties.get(pItemStack).getItemStackRenderer().renderByItem(pItemStack, pTransformType, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay);
            }

            pMatrixStack.popPose();
        }
    }

    protected void renderArmWithItem(LivingEntity p_117185_, ItemStack p_117186_, ItemTransforms.TransformType p_117187_, HumanoidArm p_117188_, PoseStack p_117189_, MultiBufferSource p_117190_, int p_117191_) {
        if (!p_117186_.isEmpty()) {
            p_117189_.pushPose();
            this.getParentModel().translateToHand(p_117188_, p_117189_);
            p_117189_.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            p_117189_.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            boolean flag = p_117188_ == HumanoidArm.LEFT;
            p_117189_.translate((float) (flag ? -1 : 1) / 16.0F, 0.125D, -0.625D);
            Minecraft.getInstance().getItemInHandRenderer().renderItem(p_117185_, p_117186_, p_117187_, flag, p_117189_, p_117190_, p_117191_);
            p_117189_.popPose();
        }
    }
}
