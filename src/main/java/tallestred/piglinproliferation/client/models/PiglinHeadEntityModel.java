package tallestred.piglinproliferation.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class PiglinHeadEntityModel extends SkullModelBase {
    private final ModelPart head;
    private final ModelPart leftEar;
    private final ModelPart rightEar;

    public PiglinHeadEntityModel(ModelPart root) {
        this.head = root.getChild("head");
        this.leftEar = this.head.getChild("left_ear");
        this.rightEar = this.head.getChild("right_ear");
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = SkullModel.createHeadModel();
        PartDefinition root = meshdefinition.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, CubeDeformation.NONE).texOffs(31, 1).addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, CubeDeformation.NONE).texOffs(2, 4).addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, CubeDeformation.NONE).texOffs(2, 0).addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, CubeDeformation.NONE), PartPose.ZERO);
        root.getChild("head").addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(51, 6).addBox(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(4.5F, -6.0F, 0.0F, 0.0F, 0.0F, (-(float) Math.PI / 6F)));
        root.getChild("head").addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(39, 6).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(-4.5F, -6.0F, 0.0F, 0.0F, 0.0F, ((float) Math.PI / 6F)));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    // I will make this cleaner
    public static LayerDefinition createAlchemistMesh() {
        MeshDefinition meshdefinition = SkullModel.createHeadModel();
        PartDefinition body = meshdefinition.getRoot();
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(62, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.25F))
                .texOffs(0, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, new CubeDeformation(-0.02F))
                .texOffs(31, 1).addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 4).addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 0).addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(51, 6).addBox(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(4.5F, -6.0F, 0.0F, 0.0F, 0.0F, (-(float) Math.PI / 6F)));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(39, 6).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(-4.5F, -6.0F, 0.0F, 0.0F, 0.0F, ((float) Math.PI / 6F)));
        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        hat.addOrReplaceChild("googles", CubeListBuilder.create().texOffs(42, 0).addBox(-5.0F, -31.0F, -5.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.1F))
                .texOffs(52, 0).addBox(1.0F, -31.0F, -5.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 120, 64);
    }

    @Override
    public void setupAnim(float animationProgress, float yaw, float pitch) {
        this.head.yRot = yaw * ((float) Math.PI / 180);
        this.head.xRot = pitch * ((float) Math.PI / 180);
        this.leftEar.zRot = (float) (-(Math.cos(animationProgress * (float) Math.PI * 0.2f * 1.2f) + 2.5)) * 0.2f;
        this.rightEar.zRot = (float) (Math.cos(animationProgress * (float) Math.PI * 0.2f) + 2.5) * 0.2f;
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        this.head.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
    }
}