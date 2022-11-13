package tallestred.piglinproliferation.common.entities.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraftforge.fml.ModList;
import tallestred.piglinproliferation.ModCompat;
import tallestred.piglinproliferation.common.entities.ai.PiglinAlchemistAi;

public class StopHoldingItemIfNoLongerAdmiringAlchemist<E extends Piglin> extends Behavior<E> {

    public StopHoldingItemIfNoLongerAdmiringAlchemist() {
        super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E piglin) {
        boolean sapienceCompat = ModList.get().isLoaded("sapience") && !PiglinTasksHelper.hasConsumableOffhandItem(piglin);
        return !piglin.getOffhandItem().isEmpty() && !piglin.getOffhandItem().canPerformAction(net.minecraftforge.common.ToolActions.SHIELD_BLOCK) && (sapienceCompat);
    }

    @Override
    protected void start(ServerLevel level, E piglin, long time) {
        if (!ModList.get().isLoaded("sapience"))
            PiglinAlchemistAi.stopHoldingOffHandItem(piglin, true);
        else
            ModCompat.stopHoldingOffHandItem(piglin, true);
    }
}
