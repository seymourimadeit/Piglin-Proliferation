package tallestred.piglinproliferation.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import tallestred.piglinproliferation.common.blockentities.PPBlockEntities;
import tallestred.piglinproliferation.common.blockentities.PiglinSkullBlockEntity;

import javax.annotation.Nullable;

public class PiglinSkullBlock extends SkullBlock {
    protected static final VoxelShape PIGLIN_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
    public final SoundEvent noteBlockSound;

    public PiglinSkullBlock(Type pType, Properties pProperties) {
        super(pType, pProperties);
        ResourceLocation soundLocation = new ResourceLocation("", "entity." + pType.getSerializedName() + ".angry");
        this.noteBlockSound = BuiltInRegistries.SOUND_EVENT.stream().filter(soundEvent -> soundEvent.getLocation().toString().contains(soundLocation.toString())).findFirst().orElseGet(() -> SoundEvent.createVariableRangeEvent(soundLocation));
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return PIGLIN_SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PiglinSkullBlockEntity(pPos, pState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, PPBlockEntities.PIGLIN_SKULL.get(), SkullBlockEntity::animation) : null;
    }

    public enum Types implements SkullBlock.Type {

        PIGLIN_BRUTE("piglin_brute"),
        PIGLIN_ALCHEMIST("piglin_alchemist"),
        ZOMBIFIED_PIGLIN("zombified_piglin"),
        PIGLIN_TRAVELLER("piglin_traveller");

        private final String name;

        Types(String name) {
            this.name = name;
            TYPES.put(name, this);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
