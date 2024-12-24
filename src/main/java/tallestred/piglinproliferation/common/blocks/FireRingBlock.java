package tallestred.piglinproliferation.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tallestred.piglinproliferation.common.blockentities.FireRingBlockEntity;
import tallestred.piglinproliferation.common.blockentities.PPBlockEntities;

import java.util.Optional;

import static tallestred.piglinproliferation.util.CodeUtilities.castOrNull;
import static tallestred.piglinproliferation.util.CodeUtilities.potionContents;

public class FireRingBlock extends CampfireBlock {
    public static final BooleanProperty BREWING = BooleanProperty.create("brewing");
    protected static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 4, 15);
    protected final int effectApplyTime;

    public FireRingBlock(boolean spawnParticles, int fireDamage, int effectApplyTime, Properties properties) {
        super(spawnParticles, fireDamage, properties);
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(BREWING, Boolean.valueOf(false))
                        .setValue(LIT, Boolean.valueOf(false))
        );
        this.effectApplyTime = effectApplyTime;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FireRingBlockEntity(pos, state);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof FireRingBlockEntity blockEntity) {
            Optional<RecipeHolder<CampfireCookingRecipe>> optional = blockEntity.getCookableRecipe(stack);
            if (optional.isPresent()) {
                if (!level.isClientSide && blockEntity.placeFood(player, player.getAbilities().instabuild ? stack.copy() : stack, 2 * ((CampfireCookingRecipe) ((RecipeHolder<?>) optional.get()).value()).getCookingTime())) {
                    player.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
                    return ItemInteractionResult.SUCCESS;
                }
                return ItemInteractionResult.CONSUME;
            } else if (stack.getItem() == Items.POTION)
                if (blockEntity.addEffects(player, hand, stack, potionContents(stack))) {
                    return ItemInteractionResult.SUCCESS;
                }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            state.setValue(BREWING, Boolean.valueOf(false));
            state.setValue(LIT, Boolean.valueOf(false));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return state.getValue(LIT) ? createTickerHelper(blockEntityType, PPBlockEntities.FIRE_RING.get(), FireRingBlockEntity::particleTick) : null;
        } else {
            return state.getValue(LIT) ? createTickerHelper(blockEntityType, PPBlockEntities.FIRE_RING.get(), (lvl, pos, st, be) -> FireRingBlockEntity.cookTick(lvl, pos, st, be, this.effectApplyTime)) : createTickerHelper(blockEntityType, PPBlockEntities.FIRE_RING.get(), FireRingBlockEntity::cooldownTick);
        }
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hitResult, Projectile projectile) {
        super.onProjectileHit(level, state, hitResult, projectile);
        if (level.getBlockEntity(hitResult.getBlockPos()) instanceof FireRingBlockEntity blockEntity && projectile instanceof ThrownPotion potion)
            blockEntity.addEffects(castOrNull(projectile.getOwner(), Player.class), null, null, potionContents(potion.getItem()));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING, BREWING);
    }
}
