package tallestred.piglinproliferation.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tallestred.piglinproliferation.common.blocks.PiglinSkullBlock;

public class PiglinSkullBlockEntity extends SkullBlockEntity {
    public PiglinSkullBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return PPBlockEntities.PIGLIN_SKULL.get();
    }
}
