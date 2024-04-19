package tallestred.piglinproliferation.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tallestred.piglinproliferation.common.blockentities.FireRingBlockEntity;
import tallestred.piglinproliferation.common.blockentities.PPBlockEntities;

import java.util.Optional;

import static tallestred.piglinproliferation.util.CodeUtilities.castOrNull;

public class FireRingBlock extends CampfireBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 5.0, 16.0);
    protected final int potionTime;

    public FireRingBlock(boolean spawnParticles, int fireDamage, int potionTime, Properties properties) {
        super(spawnParticles, fireDamage, properties);
        this.potionTime = potionTime;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FireRingBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof FireRingBlockEntity blockEntity) {
            ItemStack stack = player.getItemInHand(hand);
            Optional<RecipeHolder<CampfireCookingRecipe>> optional = blockEntity.getCookableRecipe(stack);
            if (optional.isPresent()) {
                if (!level.isClientSide && blockEntity.placeFood(player, player.getAbilities().instabuild ? stack.copy() : stack, 2 * ((CampfireCookingRecipe) ((RecipeHolder<?>) optional.get()).value()).getCookingTime())) {
                    player.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }
            else if (state.getValue(LIT) && stack.getItem() == Items.POTION)
                return blockEntity.addEffects(player, hand, stack, PotionUtils.getMobEffects(stack), this.potionTime) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
             return state.getValue(LIT) ? createTickerHelper(blockEntityType, PPBlockEntities.FIRE_RING.get(), FireRingBlockEntity::particleTick) : null;
        } else {
            return state.getValue(LIT) ? createTickerHelper(blockEntityType, PPBlockEntities.FIRE_RING.get(), FireRingBlockEntity::cookTick) : createTickerHelper(blockEntityType, PPBlockEntities.FIRE_RING.get(), FireRingBlockEntity::cooldownTick);
        }
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hitResult, Projectile projectile) {
        super.onProjectileHit(level, state, hitResult, projectile);
        if (state.getValue(LIT) && level.getBlockEntity(hitResult.getBlockPos()) instanceof FireRingBlockEntity blockEntity && projectile instanceof ThrownPotion potion)
            blockEntity.addEffects(castOrNull(projectile.getOwner(), Player.class), null, null, PotionUtils.getMobEffects(potion.getItem()), this.potionTime);
    }
}
