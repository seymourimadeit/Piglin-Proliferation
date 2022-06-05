package tallestred.piglinproliferation.common.entities.ai.goals;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class PiglinCallForHelpGoal extends Goal {
    protected final Predicate<? super AbstractPiglin> nearbyPiglinPredicate;
    private final AbstractPiglin piglin;
    private PiglinAlchemist alchemist;

    public PiglinCallForHelpGoal(AbstractPiglin abstractPiglin, Predicate<? super AbstractPiglin> nearbyPiglinPredicate) {
        this.piglin = abstractPiglin;
        this.nearbyPiglinPredicate = nearbyPiglinPredicate;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        List<PiglinAlchemist> list = this.piglin.level.getEntitiesOfClass(PiglinAlchemist.class, this.piglin.getBoundingBox().inflate(15.0D, 3.0D, 15.0D));
        if (!list.isEmpty()) {
            for (PiglinAlchemist alchemist : list) {
                if (alchemist.isAlive()) {
                    this.alchemist = alchemist;
                    return this.nearbyPiglinPredicate.test(piglin);
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
        WalkTarget walktarget = new WalkTarget(this.alchemist, (float) 1.0D, 5);
        this.piglin.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walktarget);
        //this.piglin.getNavigation().moveTo(alchemist, 1.0D);
    }

    @Override
    public void stop() {
        this.piglin.getNavigation().stop();
    }
}
