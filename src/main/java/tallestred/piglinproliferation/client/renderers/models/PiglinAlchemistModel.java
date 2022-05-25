package tallestred.piglinproliferation.client.renderers.models;

import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelPart;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

public class PiglinAlchemistModel<T extends PiglinAlchemist> extends PiglinModel<T> {
    public PiglinAlchemistModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        if (pEntity.isGonnaThrowPotion()) {
            if (pEntity.isLeftHanded()) {
                this.rightArm.xRot = -1.8F;
            } else {
                this.leftArm.xRot = -1.8F;
            }
        }
    }
}
