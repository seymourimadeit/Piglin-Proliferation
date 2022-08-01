package tallestred.piglinproliferation.common.entities.ai.behaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class SwimOnlyOutOfLava extends Swim {
    public SwimOnlyOutOfLava(float chance) {
        super(chance);
    }

    @Override
    protected void tick(ServerLevel level, Mob mob, long gameTime) {
        super.tick(level, mob, gameTime);
        Vec3 vec3 = LandRandomPos.getPos((PathfinderMob) mob, 15, 7);
        if (vec3 != null && mob.getNavigation().isDone()) {
            mob.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1.0D);
        }
    }
}
