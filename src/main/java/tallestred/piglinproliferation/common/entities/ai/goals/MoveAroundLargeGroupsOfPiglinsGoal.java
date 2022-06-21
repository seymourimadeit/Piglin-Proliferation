package tallestred.piglinproliferation.common.entities.ai.goals;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import javax.annotation.Nullable;

public class MoveAroundLargeGroupsOfPiglinsGoal extends RandomStrollGoal {
    protected final PiglinAlchemist alchemist;

    public MoveAroundLargeGroupsOfPiglinsGoal(PiglinAlchemist alchemist, double speed) {
        super(alchemist, speed, 240, false);
        this.alchemist = alchemist;
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        if (alchemist.getRandom().nextFloat() < 0.70F) {
            for (AbstractPiglin piglin : alchemist.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of())) {
                if (piglin != alchemist) {
                    if (alchemist.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of()).size() > 3) {
                        return LandRandomPos.getPosTowards(alchemist, 10, 7, piglin.position());
                    }
                }
            }
        } else {
            return null;
        }
        return null;
    }
}
