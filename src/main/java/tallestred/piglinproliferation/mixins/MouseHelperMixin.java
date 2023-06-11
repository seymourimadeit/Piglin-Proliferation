package tallestred.piglinproliferation.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tallestred.piglinproliferation.common.enchantments.PPEnchantments;
import tallestred.piglinproliferation.common.items.BucklerItem;
import tallestred.piglinproliferation.common.items.PPItems;

@Mixin(MouseHandler.class)
public class MouseHelperMixin {
    @Final
    @Shadow
    private Minecraft minecraft;

    @ModifyVariable(at = @At(value = "STORE", opcode = Opcodes.DSTORE), method = "turnPlayer", ordinal = 2)
    public double updatePlayerLook(double original) {
        return minecraft.player != null && BucklerItem.getChargeTicks(PPItems.checkEachHandForBuckler(minecraft.player)) > 0 && PPEnchantments.getBucklerEnchantsOnHands(PPEnchantments.TURNING.get(), minecraft.player) == 0 ? 0.2F : original;
    }
}
