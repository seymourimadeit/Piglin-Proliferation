package tallestred.piglinproliferation.common.entities.ai.goals;

import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import javax.annotation.Nullable;

public class RunAwayAfterThreeShots extends RandomStrollGoal {
    private final PiglinAlchemist alchemist;

    public RunAwayAfterThreeShots(PiglinAlchemist alchemist, double pSpeedModifier) {
        super(alchemist, pSpeedModifier);
        this.alchemist = alchemist;
    }

    @Override
    public boolean canUse() {
        return this.alchemist.getArrowsShot() >= 3 && this.alchemist.getTarget() != null && this.findPosition();
    }

    @Override
    public void start() {
        super.start();
        this.alchemist.setArrowsShot(0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mob.getTarget() == null)
            return;
        this.mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.mob.getTarget(), true));
        this.mob.getLookControl().setLookAt(this.mob.getTarget());
        this.mob.lookAt(this.mob.getTarget(), 30.0f, 30.0F);
    }

    public boolean findPosition() {
        Vec3 vector3d = this.getPosition();
        if (vector3d == null) {
            return false;
        } else {
            this.wantedX = vector3d.x;
            this.wantedY = vector3d.y;
            this.wantedZ = vector3d.z;
            return true;
        }
    }

    @Override
    @Nullable
    protected Vec3 getPosition() {
        return LandRandomPos.getPosAway(this.mob, 16, 7, this.mob.getTarget().position());
    }
}
