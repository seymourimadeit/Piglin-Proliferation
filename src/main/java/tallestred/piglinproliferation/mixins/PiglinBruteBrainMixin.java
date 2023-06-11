package tallestred.piglinproliferation.mixins;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.piglin.PiglinBruteAi;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestred.piglinproliferation.common.entities.ai.behaviors.ChargeTask;

@Mixin(PiglinBruteAi.class)
public class PiglinBruteBrainMixin {
    @Inject(at = @At(value = "HEAD"), cancellable = true, method = "initFightActivity")
    private static void initFightActivity(PiglinBrute brute, Brain<PiglinBrute> brain, CallbackInfo info) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(new ChargeTask<>()), MemoryModuleType.ATTACK_TARGET);
    }
}
