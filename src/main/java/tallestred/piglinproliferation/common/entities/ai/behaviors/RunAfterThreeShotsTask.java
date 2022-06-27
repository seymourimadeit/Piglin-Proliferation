package tallestred.piglinproliferation.common.entities.ai.behaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import javax.annotation.Nullable;

public class RunAfterThreeShotsTask extends RandomStroll {
    private int runTime;
    private boolean startedRunning;

    public RunAfterThreeShotsTask(float speedModifier) {
        super(speedModifier);
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob mob, long gameTime) {
        super.start(level, mob, gameTime);
        this.runTime = 60;
        this.startedRunning = true;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob mob) {
        PiglinAlchemist alchemist = (PiglinAlchemist) mob;
        return alchemist.getArrowsShot() > 3 && alchemist.getTarget() != null && this.getTargetPos(mob) != null && !startedRunning;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob mob, long gameTime) {
        return super.canStillUse(level, mob, gameTime) && this.runTime > 0;
    }

    @Override
    protected void tick(ServerLevel level, PathfinderMob mob, long gameTime) {
        if (mob.getTarget() == null)
            return;
        this.runTime--;
        mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(mob.getTarget(), true));
        mob.getLookControl().setLookAt(mob.getTarget());
        mob.lookAt(mob.getTarget(), 30.0f, 30.0F);
        if (this.runTime <= 0) {
            this.startedRunning = false;
            mob.getNavigation().stop();
        }
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob mob, long gameTime) {
        super.stop(level, mob, gameTime);
        PiglinAlchemist alchemist = (PiglinAlchemist) mob;
        this.runTime = 0;
        this.startedRunning = false;
        alchemist.setArrowsShot(0);
    }

    @Override
    protected Vec3 getTargetPos(PathfinderMob mob) {
        return LandRandomPos.getPosAway(mob, 8, 7, mob.getTarget().position());
    }
}
