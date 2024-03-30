package tallestred.piglinproliferation.common.entities.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;

public class TravellerSit<T extends PiglinTraveller> extends Behavior<T> {
    public TravellerSit() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT), 1200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, T owner) {
        return true;
    }

    @Override
    protected void tick(ServerLevel pLevel, T pOwner, long pGameTime) {
        super.tick(pLevel, pOwner, pGameTime);
        pOwner.getNavigation().stop();
        pOwner.getBrain().eraseMemory(MemoryModuleType.PATH);
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, T entityIn, long gameTimeIn) {
        return entityIn.isSitting() && !entityIn.isAggressive() && !entityIn.getBrain().hasMemoryValue(MemoryModuleType.INTERACTION_TARGET);
    }

    @Override
    protected void start(ServerLevel worldIn, T entityIn, long gameTimeIn) {
        entityIn.sit(true);
    }

    @Override
    protected void stop(ServerLevel worldIn, T entityIn, long gameTimeIn) {
        entityIn.sit(false);
    }
}


