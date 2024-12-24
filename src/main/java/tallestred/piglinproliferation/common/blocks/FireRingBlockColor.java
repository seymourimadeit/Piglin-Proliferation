package tallestred.piglinproliferation.common.blocks;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tallestred.piglinproliferation.common.blockentities.FireRingBlockEntity;

public class FireRingBlockColor implements BlockColor {
    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (level != null && pos != null) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FireRingBlockEntity fireRingBlock) {
                if (fireRingBlock.hasEffects) {
                    return fireRingBlock.potionColor;
                }
            }
        }
        return 0xffffff;
    }
}
