package tallestred.piglinproliferation.common.entities.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.piglin.Piglin;
import tallestred.piglinproliferation.common.entities.ai.PiglinAlchemistAi;

public class StopHoldingItemIfNoLongerAdmiringAlchemist<E extends Piglin> extends Behavior<E> {

    public StopHoldingItemIfNoLongerAdmiringAlchemist() {
        super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_35255_, E p_35256_) {
        return !p_35256_.getOffhandItem().isEmpty() && !p_35256_.getOffhandItem().canPerformAction(net.minecraftforge.common.ToolActions.SHIELD_BLOCK);
    }

    @Override
    protected void start(ServerLevel p_35258_, E p_35259_, long p_35260_) {
        PiglinAlchemistAi.stopHoldingOffHandItem(p_35259_, true);
    }
}
