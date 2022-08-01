package tallestred.piglinproliferation.client.renderers.models;

import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

// Made with Blockbench 4.2.5
// Exported for Minecraft version 1.17 - 1.18 with Mojang mappings
public class PiglinAlchemistModel<T extends PiglinAlchemist> extends PiglinModel<T> {
    public final ModelPart bodyForLeggings;
    public final ModelPart bodyForChest;
    public final ModelPart belt;

    public PiglinAlchemistModel(ModelPart root) {
        super(root);
        this.bodyForLeggings = this.body.getChild("bodyForLeggings");
        this.bodyForChest = this.body.getChild("bodyForChest");
        this.belt = this.body.getChild("belt");
        this.bodyForLeggings.visible = false;
    }

    public static MeshDefinition createBodyLayer(CubeDeformation bodyDeformation, CubeDeformation bodyDeformation2, CubeDeformation bodyDeformation3) {
        MeshDefinition meshdefinition = PiglinModel.createMesh(CubeDeformation.NONE);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(56, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 24.0F, 4.0F, bodyDeformation), PartPose.offset(0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("bodyForLeggings", CubeListBuilder.create()
                .texOffs(56, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 24.0F, 4.0F, bodyDeformation2), PartPose.offset(0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("bodyForChest", CubeListBuilder.create()
                .texOffs(56, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 24.0F, 4.0F, bodyDeformation3), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(62, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.25F))
                .texOffs(0, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, new CubeDeformation(-0.02F))
                .texOffs(31, 1).addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 4).addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 0).addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        hat.addOrReplaceChild("googles", CubeListBuilder.create().texOffs(42, 0).addBox(-5.0F, -31.0F, -5.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.1F))
                .texOffs(52, 0).addBox(1.0F, -31.0F, -5.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return meshdefinition;
    }

    @Override
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        boolean isWearingChestplateOrLeggings = pEntity.getItemBySlot(EquipmentSlot.CHEST)
                .getItem() instanceof ArmorItem
                || pEntity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ArmorItem;
        this.bodyForLeggings.visible = pEntity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ArmorItem && !(pEntity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorItem);
        this.bodyForChest.visible = pEntity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorItem;
        this.belt.visible = !isWearingChestplateOrLeggings;
        if (pEntity.isGonnaThrowPotion()) {
            if (pEntity.isLeftHanded()) {
                this.rightArm.xRot = -1.8F;
            } else {
                this.leftArm.xRot = -1.8F;
            }
        }
    }
}
