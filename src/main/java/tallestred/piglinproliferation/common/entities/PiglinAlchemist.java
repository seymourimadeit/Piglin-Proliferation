package tallestred.piglinproliferation.common.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.level.Level;

public class PiglinAlchemist extends AbstractPiglin {
    public PiglinAlchemist(EntityType<? extends PiglinAlchemist> p_34683_, Level p_34684_) {
        super(p_34683_, p_34684_);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0D).add(Attributes.MOVEMENT_SPEED, (double) 0.35F).add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    protected boolean canHunt() {
        return false;
    }

    @Override
    public PiglinArmPose getArmPose() {
        return null;
    }

    @Override
    protected void playConvertedSound() {

    }
}
