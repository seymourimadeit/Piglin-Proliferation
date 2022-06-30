package tallestred.piglinproliferation.common.entities.ai.behaviors;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MoveAroundPiglins extends RandomStroll {
    public MoveAroundPiglins(float speed) {
        super(speed);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob mob) {
        return super.checkExtraStartConditions(level, mob) && mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS) != null;
    }

    @Nullable
    @Override
    protected Vec3 getTargetPos(PathfinderMob alchemist) {
        for (AbstractPiglin piglin : alchemist.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of())) {
            if (alchemist.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of()).size() >= 2) {
                return LandRandomPos.getPosTowards(alchemist, 4, 3, piglin.position());
            } else {
                return null;
            }
        }
        return null;
    }
}
