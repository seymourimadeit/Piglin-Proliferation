package tallestred.piglinproliferation.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.client.renderers.layers.BeltRenderLayer;
import tallestred.piglinproliferation.client.models.PiglinAlchemistModel;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

public class PiglinAlchemistRenderer extends PiglinRenderer {
    public PiglinAlchemistRenderer(EntityRendererProvider.Context context) {
        super(context, PPRenderSetupEvents.PIGLIN_ALCHEMIST, ModelLayers.PIGLIN_INNER_ARMOR, PPRenderSetupEvents.ALCHEMIST_ARMOR_OUTER_LAYER, false);
        this.model = new PiglinAlchemistModel(context.bakeLayer(PPRenderSetupEvents.PIGLIN_ALCHEMIST));
        this.addLayer(new BeltRenderLayer(this, context.getItemInHandRenderer()));
    }

    private static HumanoidModel.ArmPose getArmPose(PiglinAlchemist mob, InteractionHand hand) {
        ItemStack itemstack = mob.getItemInHand(hand);
        HumanoidModel.ArmPose pose = HumanoidModel.ArmPose.EMPTY;
        if (!mob.swinging && !mob.isGonnaThrowPotion() && mob.isAggressive()) {
            if (mob.isHolding((stack) -> stack.getItem() instanceof CrossbowItem) && CrossbowItem.isCharged(itemstack))
                pose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
            if (mob.isHolding((stack) -> stack.getItem() instanceof BowItem) && mob.getDeltaMovement().y() <= 0 && mob.getDeltaMovement().x() <= 0 && mob.getDeltaMovement().z() <= 0)
                pose = HumanoidModel.ArmPose.BOW_AND_ARROW;
        }
        if (mob.getUsedItemHand() == hand && mob.getUseItemRemainingTicks() > 0) {
            UseAnim useanim = itemstack.getUseAnimation();
            pose = switch (useanim) {
                case BOW -> HumanoidModel.ArmPose.BOW_AND_ARROW;
                case CROSSBOW -> HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                case BLOCK -> HumanoidModel.ArmPose.BLOCK;
                case SPYGLASS -> HumanoidModel.ArmPose.SPYGLASS;
                case SPEAR -> HumanoidModel.ArmPose.THROW_SPEAR;
                default -> HumanoidModel.ArmPose.EMPTY;
            };
        }
        return pose;
    }

    @Override
    public void render(Mob pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        this.setBipedArmPoses((PiglinAlchemist) pEntity);
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    protected void setBipedArmPoses(PiglinAlchemist mob) {
        HumanoidModel.ArmPose humanoidmodel$armpose = getArmPose(mob, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose humanoidmodel$armpose1 = getArmPose(mob, InteractionHand.OFF_HAND);
        if (humanoidmodel$armpose.isTwoHanded())
            humanoidmodel$armpose1 = mob.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        if (mob.getMainArm() == HumanoidArm.RIGHT) {
            this.getModel().rightArmPose = humanoidmodel$armpose;
            this.getModel().leftArmPose = humanoidmodel$armpose1;
        } else {
            this.getModel().rightArmPose = humanoidmodel$armpose1;
            this.getModel().leftArmPose = humanoidmodel$armpose;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Mob pEntity) {
        return new ResourceLocation(PiglinProliferation.MODID, "textures/entity/piglin/alchemist/alchemist.png");
    }
}
