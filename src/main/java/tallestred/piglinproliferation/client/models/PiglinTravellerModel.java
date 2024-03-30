package tallestred.piglinproliferation.client.models;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;

public class PiglinTravellerModel<T extends PiglinTraveller> extends PiglinModel<T> {
    public final ModelPart jacket;
    public final ModelPart hat_brim;

    public PiglinTravellerModel(ModelPart root) {
        super(root);
        this.jacket = this.body.getChild("jacket");
        this.hat_brim = this.getHead().getChild("hat_brim");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = PiglinModel.createMesh(CubeDeformation.NONE);
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 24.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -24.0F, 0.0F));
        body.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(56, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(62, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -24.0F, 0.0F));
        head.addOrReplaceChild("hat_brim", CubeListBuilder.create().texOffs(80, 19).addBox(-8.0F, -5.0F, -8.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("hat_big", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, new CubeDeformation(-0.02F))
                .texOffs(31, 1).addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 4).addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 0).addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 148, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (entity.isSitting()) {
            this.rightArm.xRot += (-(float) Math.PI / 5F);
            this.leftArm.xRot += (-(float) Math.PI / 5F);
            this.rightLeg.xRot = -1.4137167F;
            this.rightLeg.yRot = ((float) Math.PI / 10F);
            this.rightLeg.zRot = 0.07853982F;
            this.leftLeg.xRot = -1.4137167F;
            this.leftLeg.yRot = (-(float) Math.PI / 10F);
            this.leftLeg.zRot = -0.07853982F;
        }
        this.hat_brim.visible = entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        if (entity.getKickTicks() > 0) {
            float f1 = 1.0F - (float) Mth.abs(10 - 2 * entity.getKickTicks()) / 10.0F;
            this.rightLeg.xRot = Mth.lerp(f1, this.rightLeg.xRot, -1.40F);
        }
    }
}
