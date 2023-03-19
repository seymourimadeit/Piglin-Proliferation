package tallestred.piglinproliferation.common.entities.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.StopHoldingItemIfNoLongerAdmiring;
import net.minecraftforge.fml.ModList;
import tallestred.piglinproliferation.ModCompat;
import tallestred.piglinproliferation.common.entities.ai.PiglinAlchemistAi;

public class StopHoldingItemIfNoLongerAdmiringAlchemist<E extends Piglin> extends StopHoldingItemIfNoLongerAdmiring<E> {

    public StopHoldingItemIfNoLongerAdmiringAlchemist() {
    }

    @Override
    protected void start(ServerLevel level, E piglin, long time) {
        if (!ModList.get().isLoaded("sapience"))
            PiglinAlchemistAi.stopHoldingOffHandItem(piglin, true);
        else
            ModCompat.stopHoldingOffHandItem(piglin, true);
    }
}