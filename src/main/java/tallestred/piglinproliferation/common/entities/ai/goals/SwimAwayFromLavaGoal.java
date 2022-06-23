package tallestred.piglinproliferation.common.entities.ai.goals;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class SwimAwayFromLavaGoal extends WaterAvoidingRandomStrollGoal {
    public SwimAwayFromLavaGoal(PathfinderMob p_25987_, double p_25988_) {
        super(p_25987_, p_25988_);
    }

    @Nullable
    protected Vec3 getPosition() {
        if (this.mob.isInLava()) {
            Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7);
            return vec3 == null ? super.getPosition() : vec3;
        } else {
            return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 10, 7) : super.getPosition();
        }
    }
}