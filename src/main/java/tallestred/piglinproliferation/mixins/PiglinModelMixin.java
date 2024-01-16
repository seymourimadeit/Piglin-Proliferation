package tallestred.piglinproliferation.mixins;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinModel.class)
public abstract class PiglinModelMixin<T extends Mob> extends PlayerModel<T> {

    public PiglinModelMixin(ModelPart modelPart) {
        super(modelPart, false);
    }


    @Override
    public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
        super.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
        if (pEntity instanceof ZombifiedPiglin)
            doArmPoses(pEntity);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZFF)V"), cancellable = true, method = "setupAnim")
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo info) {
        if (rightArmPose != ArmPose.EMPTY || leftArmPose != ArmPose.EMPTY) {
            info.cancel();
        }
    }

    private void doArmPoses(T entityIn) {
        ItemStack itemstack = entityIn.getMainHandItem();
        ItemStack itemstack1 = entityIn.getOffhandItem();
        HumanoidModel.ArmPose bipedmodel$armpose = this.getArmPose(entityIn, itemstack, itemstack1,
                InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose bipedmodel$armpose1 = this.getArmPose(entityIn, itemstack, itemstack1,
                InteractionHand.OFF_HAND);
        if (entityIn.getMainArm() == HumanoidArm.RIGHT) {
            this.rightArmPose = bipedmodel$armpose;
            this.leftArmPose = bipedmodel$armpose1;
        } else {
            this.rightArmPose = bipedmodel$armpose1;
            this.leftArmPose = bipedmodel$armpose;
        }
    }

    private HumanoidModel.ArmPose getArmPose(T entityIn, ItemStack itemStackMain, ItemStack itemStackOff,
                                             InteractionHand handIn) {
        HumanoidModel.ArmPose bipedmodel$armpose = HumanoidModel.ArmPose.EMPTY;
        ItemStack itemstack = handIn == InteractionHand.MAIN_HAND ? itemStackMain : itemStackOff;
        if (!itemstack.isEmpty()) {
            if (entityIn.getUseItemRemainingTicks() > 0) {
                UseAnim useaction = itemstack.getUseAnimation();
                switch (useaction) {
                    case BLOCK:
                        bipedmodel$armpose = HumanoidModel.ArmPose.BLOCK;
                        break;
                    case BOW:
                        bipedmodel$armpose = HumanoidModel.ArmPose.BOW_AND_ARROW;
                        break;
                    case SPEAR:
                        bipedmodel$armpose = HumanoidModel.ArmPose.THROW_SPEAR;
                        break;
                    case CROSSBOW:
                        if (handIn == entityIn.getUsedItemHand()) {
                            bipedmodel$armpose = HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                        }
                        break;
                    default:
                        bipedmodel$armpose = HumanoidModel.ArmPose.EMPTY;
                        break;
                }
            } else {
                boolean flag1 = itemStackMain.getItem() instanceof CrossbowItem;
                boolean flag2 = itemStackOff.getItem() instanceof CrossbowItem;
                if (flag1) {
                    bipedmodel$armpose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }

                if (flag2 && itemStackMain.getItem().getUseAnimation(itemStackMain) == UseAnim.NONE) {
                    bipedmodel$armpose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }
            }
        }
        return bipedmodel$armpose;
    }
}
