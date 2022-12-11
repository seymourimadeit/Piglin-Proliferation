package tallestred.piglinproliferation.common.entities.ai.behaviors;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class MoveAroundPiglins extends RandomStroll {

    public static OneShot<PathfinderMob> moveAroundPiglins(float p_260303_, boolean p_259639_) {
        return strollFlyOrSwim(p_260303_, (mob) -> {
            return mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS) != null ? getTargetPos(mob) : LandRandomPos.getPos(mob, 10, 7);
        }, p_259639_ ? (p_258615_) -> {
            return true;
        } : (p_258613_) -> {
            return !p_258613_.isInWaterOrBubble();
        });
    }

    private static OneShot<PathfinderMob> strollFlyOrSwim(float p_260030_, Function<PathfinderMob, Vec3> p_259912_, Predicate<PathfinderMob> p_259088_) {
        return BehaviorBuilder.create((p_258620_) -> {
            return p_258620_.group(p_258620_.absent(MemoryModuleType.WALK_TARGET)).apply(p_258620_, (p_258600_) -> {
                return (p_258610_, p_258611_, p_258612_) -> {
                    if (!p_259088_.test(p_258611_)) {
                        return false;
                    } else {
                        Optional<Vec3> optional = Optional.ofNullable(p_259912_.apply(p_258611_));
                        p_258600_.setOrErase(optional.map((p_258622_) -> {
                            return new WalkTarget(p_258622_, p_260030_, 0);
                        }));
                        return true;
                    }
                };
            });
        });
    }


    @Nullable
    protected static Vec3 getTargetPos(PathfinderMob alchemist) {
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
