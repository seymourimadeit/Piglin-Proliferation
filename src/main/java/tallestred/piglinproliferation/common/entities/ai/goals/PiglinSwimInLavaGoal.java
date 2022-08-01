package tallestred.piglinproliferation.common.entities.ai.goals;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.phys.Vec3;

public class PiglinSwimInLavaGoal extends FloatGoal {
    protected AbstractPiglin piglin;

    public PiglinSwimInLavaGoal(Mob mob) {
        super(mob);
        this.piglin = (AbstractPiglin) mob;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 vec3 = LandRandomPos.getPos(piglin, 15, 7);
        if (vec3 != null && piglin.getNavigation().isDone())
            piglin.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1.0D);
    }
}
