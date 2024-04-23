package tallestred.piglinproliferation.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tallestred.piglinproliferation.common.attribute.PPAttributes;

@Mixin(MouseHandler.class)
public class MouseHelperMixin {
    @Final
    @Shadow
    private Minecraft minecraft;

    @ModifyVariable(at = @At(value = "STORE", opcode = Opcodes.DSTORE), method = "turnPlayer", ordinal = 2)
    public double updatePlayerLook(double mouseSensitivity) {
        if (minecraft.player != null) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                double turningValue = PPAttributes.turningValue(player);
                if (turningValue != 1)
                    mouseSensitivity = (mouseSensitivity * turningValue) - 0.20000000298023224;
                //TODO this doesn't work right
            }
        }
        return mouseSensitivity;
    }
}
