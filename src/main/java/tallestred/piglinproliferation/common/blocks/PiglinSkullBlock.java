package tallestred.piglinproliferation.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.blockentities.PPBlockEntities;
import tallestred.piglinproliferation.common.blockentities.PiglinSkullBlockEntity;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;
import tallestred.piglinproliferation.util.LazyLoadedArray;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

import static tallestred.piglinproliferation.util.CodeUtilities.addToArray;
import static tallestred.piglinproliferation.util.CodeUtilities.castOrThrow;

@SuppressWarnings("unchecked")
public class PiglinSkullBlock extends SkullBlock {
    public static final LazyLoadedArray<AbstractSkullBlock> PIGLIN_HEADS = new LazyLoadedArray<>(8);
    protected static final VoxelShape PIGLIN_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);

    public PiglinSkullBlock(Type pType, Properties pProperties) {
        super(pType, pProperties);
        PIGLIN_HEADS.add(Holder.direct(this));
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return PIGLIN_SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PiglinSkullBlockEntity(pPos, pState);
    }

    @Override
    public Types getType() {
        return (Types) super.getType();
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, PPBlockEntities.PIGLIN_SKULL.get(), SkullBlockEntity::animation) : null;
    }

    public enum Types implements SkullBlock.Type {

        PIGLIN_BRUTE("piglin_brute", () -> EntityType.PIGLIN_BRUTE, PPSounds.NOTE_BLOCK_IMITATE_PIGLIN_BRUTE),
        PIGLIN_ALCHEMIST("piglin_alchemist", PPEntityTypes.PIGLIN_ALCHEMIST::get, PPSounds.NOTE_BLOCK_IMITATE_PIGLIN_ALCHEMIST),
        ZOMBIFIED_PIGLIN("zombified_piglin", () -> EntityType.ZOMBIFIED_PIGLIN, PPSounds.NOTE_BLOCK_IMITATE_ZOMBIFIED_PIGLIN),
        PIGLIN_TRAVELLER("piglin_traveller", PPEntityTypes.PIGLIN_TRAVELLER::get, PPSounds.NOTE_BLOCK_IMITATE_PIGLIN_TRAVELLER);

        private final String name;
        private final Supplier<EntityType<?>> entityType;
        private final Supplier<SoundEvent> noteBlockSound;

        Types(String name, Supplier<EntityType<?>> entityType, Supplier<SoundEvent> noteBlockSound) {
            this.name = name;
            this.entityType = entityType;
            this.noteBlockSound = noteBlockSound;
            TYPES.put(name, this);
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public EntityType<?> getEntityType() {
            return entityType.get();
        }

        public SoundEvent getSoundEvent() {
            return noteBlockSound.get();
        }
    }

    public static void spawnSkullIfValidKill(DamageSource source, Entity killed, Function<Entity, Item> getItemIfValid) {
        if (source.getEntity() instanceof Creeper creeper) {
            if (creeper.canDropMobsSkull()) {
                Item spawnItem = getItemIfValid.apply(killed);
                if (spawnItem != null) {
                    killed.spawnAtLocation(spawnItem);
                    creeper.increaseDroppedSkulls();

                }
            }
        }
        if (source.getDirectEntity() instanceof Fireball fireball && fireball.getOwner() instanceof Ghast) {
            Item spawnItem = getItemIfValid.apply(killed);
            if (spawnItem != null)
                killed.spawnAtLocation(spawnItem);
        }
    }
}
