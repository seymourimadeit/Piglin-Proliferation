package tallestred.piglinproliferation.common.blocks;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import tallestred.piglinproliferation.common.blockentities.PiglinSkullBlockEntity;

import java.util.Map;

public class PiglinWallSkullBlock extends WallSkullBlock {
    private static final Map<Direction, VoxelShape> SHAPES = Maps.immutableEnumMap(Map.of(Direction.NORTH, Block.box(3.0, 4.0, 8.0, 13.0, 12.0, 16.0), Direction.SOUTH, Block.box(3.0, 4.0, 0.0, 13.0, 12.0, 8.0), Direction.EAST, Block.box(0.0, 4.0, 3.0, 8.0, 12.0, 13.0), Direction.WEST, Block.box(8.0, 4.0, 3.0, 16.0, 12.0, 13.0)));

    public PiglinWallSkullBlock(SkullBlock.Type pType, Properties pProperties) {
        super(pType, pProperties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PiglinSkullBlockEntity(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPES.get(pState.getValue(FACING));
    }
}
