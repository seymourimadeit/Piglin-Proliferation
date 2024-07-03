package tallestred.piglinproliferation.common.entities.ai.behaviors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.StopHoldingItemIfNoLongerAdmiring;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.ItemAbilities;
import tallestred.piglinproliferation.common.entities.ai.AbstractPiglinAi;

public class StopHoldingItemAfterAdmiring<E extends Piglin> extends StopHoldingItemIfNoLongerAdmiring {
    public static<P extends Piglin> BehaviorControl<P> create(AbstractPiglinAi<P> aiInstance, ResourceKey<LootTable> lootTableLocation) {
        return BehaviorBuilder.create((p_259197_) -> {
            return p_259197_.group(p_259197_.absent(MemoryModuleType.ADMIRING_ITEM)).apply(p_259197_, (p_259512_) -> {
                return (p_259681_, piglin, p_259451_) -> {
                    if (!piglin.getOffhandItem().isEmpty() && !piglin.getOffhandItem().canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
                            aiInstance.stopHoldingOffHandItem(piglin, true, lootTableLocation);
                    //    else if (BehaviorHelper.canStopHoldingItemIfNoLongerAdmiring(!piglin.getOffhandItem().isEmpty() && !piglin.getOffhandItem().canPerformAction(net.minecraftforge.common.ToolActions.SHIELD_BLOCK), piglin))
                    //        ModCompat.stopHoldingOffHandItem(piglin, true);
                        return true;
                    } else {
                        return false;
                    }
                };
            });
        });
    }
}
