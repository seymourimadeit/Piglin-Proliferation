package tallestred.piglinproliferation.client.renderers.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.client.PPClientEvents;
import tallestred.piglinproliferation.client.renderers.models.PiglinAlchemistModel;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;
import tallestred.piglinproliferation.configuration.PPConfig;

import static net.minecraft.client.renderer.entity.ItemRenderer.*;

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
                this.renderBeltItems(pLivingEntity, itemstack, ItemTransforms.TransformType.GROUND, pMatrixStack, pBuffer, pAgeInTicks, pPackedLight, inventorySlots);
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
        coloredCutoutModelCopyLayerRender(this.getParentModel(), layerModel, new ResourceLocation(PiglinProliferation.MODID, "textures/entity/piglin/alchemist/belt.png"), pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, pPartialTicks, 1.0F, 1.0F, 1.0F);
    }

    protected void renderBeltItems(LivingEntity entity, ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource source, float ageInTicks, int light, int inventorySlot) {
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
                double secondSlotInflation = entity.hasItemInSlot(EquipmentSlot.LEGS) && !entity.hasItemInSlot(EquipmentSlot.CHEST) ? -0.290D : entity.hasItemInSlot(EquipmentSlot.CHEST) ? -0.265D : -0.325D;
                double fifthSlotInflation = entity.hasItemInSlot(EquipmentSlot.LEGS) && !entity.hasItemInSlot(EquipmentSlot.CHEST) ? -2.310D : entity.hasItemInSlot(EquipmentSlot.CHEST) ? -2.335D : -2.275D;
                poseStack.translate(-0.2D, 0.4D, inventorySlot == 2 ? secondSlotInflation : fifthSlotInflation);
            }
            poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
            BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, entity.level, entity, light);
            this.renderItemsBelt(stack, transformType, false, poseStack, source, light, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), model, Minecraft.getInstance().getItemRenderer());
            poseStack.popPose();
        }
    }

    // Yes I did infact shamelessly copy ItemRenderer#render for this and made it not render glowing in the worst way possible.
    public void renderItemsBelt(ItemStack itemStack, ItemTransforms.TransformType transformType, boolean leftHandTransforms, PoseStack poseStack, MultiBufferSource source, int light, int overLay, BakedModel bakedModel, ItemRenderer renderer) {
        if (!itemStack.isEmpty()) {
            poseStack.pushPose();
            boolean flag = transformType == ItemTransforms.TransformType.GUI || transformType == ItemTransforms.TransformType.GROUND || transformType == ItemTransforms.TransformType.FIXED;
            if (flag) {
                if (itemStack.is(Items.TRIDENT)) {
                    bakedModel = renderer.getItemModelShaper().getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
                } else if (itemStack.is(Items.SPYGLASS)) {
                    bakedModel = renderer.getItemModelShaper().getModelManager().getModel(new ModelResourceLocation("minecraft:spyglass#inventory"));
                }
            }

            bakedModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(poseStack, bakedModel, transformType, leftHandTransforms);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
            if (!bakedModel.isCustomRenderer() && (!itemStack.is(Items.TRIDENT) || flag)) {
                boolean flag1;
                if (transformType != ItemTransforms.TransformType.GUI && !transformType.firstPerson() && itemStack.getItem() instanceof BlockItem) {
                    Block block = ((BlockItem)itemStack.getItem()).getBlock();
                    flag1 = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
                } else {
                    flag1 = true;
                }
                for (var model : bakedModel.getRenderPasses(itemStack, flag1)) {
                    for (var rendertype : model.getRenderTypes(itemStack, flag1)) {
                        VertexConsumer vertexconsumer;
                        if (itemStack.is(ItemTags.COMPASSES) && itemStack.hasFoil()) {
                            poseStack.pushPose();
                            PoseStack.Pose posestack$pose = poseStack.last();
                            if (transformType == ItemTransforms.TransformType.GUI) {
                                posestack$pose.pose().multiply(0.5F);
                            } else if (transformType.firstPerson()) {
                                posestack$pose.pose().multiply(0.75F);
                            }

                            if (flag1) {
                                vertexconsumer = getCompassFoilBufferDirect(source, rendertype, posestack$pose);
                            } else {
                                vertexconsumer = getCompassFoilBuffer(source, rendertype, posestack$pose);
                            }

                            poseStack.popPose();
                        } else if (flag1) {
                            vertexconsumer = getFoilBufferDirect(source, rendertype, true, itemStack.hasFoil() && PPConfig.CLIENT.beltTextureGlow.get());
                        } else {
                            vertexconsumer = getFoilBuffer(source, rendertype, true, itemStack.hasFoil() && PPConfig.CLIENT.beltTextureGlow.get());
                        }

                        renderer.renderModelLists(model, itemStack, light, overLay, poseStack, vertexconsumer);
                    }
                }
            } else {
                net.minecraftforge.client.extensions.common.IClientItemExtensions.of(itemStack).getCustomRenderer().renderByItem(itemStack, transformType, poseStack, source, light, overLay);
            }

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
