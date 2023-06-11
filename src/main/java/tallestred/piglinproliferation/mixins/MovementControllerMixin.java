package tallestred.piglinproliferation.mixins;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestred.piglinproliferation.common.enchantments.PPEnchantments;
import tallestred.piglinproliferation.common.items.BucklerItem;
import tallestred.piglinproliferation.common.items.PPItems;

@Mixin(MoveControl.class)
public abstract class MovementControllerMixin {
    @Shadow
    @Final
    protected Mob mob;

    @Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo info) {
        if (PPEnchantments.getBucklerEnchantsOnHands(PPEnchantments.TURNING.get(), mob) >= 0 && BucklerItem.getChargeTicks(PPItems.checkEachHandForBuckler(mob)) > 0)
            info.cancel();
    }
}
