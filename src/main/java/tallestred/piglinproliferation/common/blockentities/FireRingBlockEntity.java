package tallestred.piglinproliferation.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import tallestred.piglinproliferation.common.blocks.FireRingBlock;

import javax.annotation.Nullable;
import java.util.*;

public class FireRingBlockEntity extends CampfireBlockEntity {
    private final List<MobEffectInstance> effects = new ArrayList<>();
    private int unsafeLightLevel = -1;
    private int potionColor = -1;

    public FireRingBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    private int getLightLevel() {
        if (this.unsafeLightLevel < 0) {
            BlockState state = this.getBlockState();
            BlockPos pos = this.getBlockPos();
            this.unsafeLightLevel = state.getBlock().getLightEmission(state, this.level, pos);
        }
        return this.unsafeLightLevel;
    }

    public boolean addEffects(@Nullable Player player, @Nullable InteractionHand hand, @Nullable ItemStack stack, List<MobEffectInstance> effectsToAdd, int maxDuration) {
        if (this.effects.isEmpty() && !effectsToAdd.isEmpty()) {
            this.potionColor = PotionUtils.getColor(effectsToAdd);
            if (this.level != null && !this.level.isClientSide)
                this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
            if (player != null) {
                if (!player.getAbilities().instabuild && stack != null) {
                    stack.shrink(1);
                    ItemStack result = ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE));
                    if (stack.isEmpty() && hand != null)
                        player.setItemInHand(hand, result);
                }
            }
            for (MobEffectInstance effectInstance : effectsToAdd)
                this.effects.add(new MobEffectInstance(effectInstance.getEffect(), Math.min(effectInstance.getDuration(), maxDuration), effectInstance.getAmplifier()));
            if (!(this.level == null))
                this.level.playSound(null, this.getBlockPos(), SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            this.setChanged();
            return true;
        }
        return false;
    }

    public static void particleTick(Level level, BlockPos pos, BlockState state, FireRingBlockEntity blockEntity) {
        if (blockEntity.potionColor != -1) {
            double xComponent = (double)(blockEntity.potionColor >> 16 & 255) / 255.0;
            double yComponent = (double)(blockEntity.potionColor >> 8 & 255) / 255.0;
            double zComponent = (double)(blockEntity.potionColor >> 0 & 255) / 255.0;
            RandomSource randomSource = level.getRandom();
            level.addAlwaysVisibleParticle(randomSource.nextBoolean() ? ParticleTypes.ENTITY_EFFECT : ParticleTypes.AMBIENT_ENTITY_EFFECT, true, (double)pos.getX() + 0.5 + randomSource.nextDouble() / 3.0 * (double)(randomSource.nextBoolean() ? 1 : -1), (double)pos.getY() + randomSource.nextDouble() + randomSource.nextDouble(), (double)pos.getZ() + 0.5 + randomSource.nextDouble() / 3.0 * (double)(randomSource.nextBoolean() ? 1 : -1), xComponent, yComponent, zComponent);
        }
        CampfireBlockEntity.particleTick(level, pos, state, blockEntity);
    }

    public static void cookTick(Level level, BlockPos pos, BlockState state, FireRingBlockEntity blockEntity) {
        potionTick(level, pos, state, blockEntity);
        CampfireBlockEntity.cookTick(level, pos, state, blockEntity);
    }

    public static void cooldownTick(Level level, BlockPos pos, BlockState state, FireRingBlockEntity blockEntity) {
        potionTick(level, pos, state, blockEntity);
        CampfireBlockEntity.cooldownTick(level, pos, state, blockEntity);
    }

    public static void potionTick(Level level, BlockPos pos, BlockState state, FireRingBlockEntity blockEntity) {
        if (!blockEntity.effects.isEmpty()) {
            if (!state.getValue(FireRingBlock.LIT))
                blockEntity.effects.clear();
            else for (MobEffectInstance effectInstance : new ArrayList<>(blockEntity.effects)) {
                effectInstance.tickDownDuration();
                if (!effectInstance.isInfiniteDuration() && effectInstance.getDuration() <= 0)
                    blockEntity.effects.remove(effectInstance);
            }
            blockEntity.potionColor = blockEntity.effects.isEmpty() ? -1 : PotionUtils.getColor(blockEntity.effects);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            blockEntity.setChanged();
            double radius = (blockEntity.getLightLevel() / 1.3);
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius)))
                blockEntity.effects.forEach(effect -> entity.addEffect(new MobEffectInstance(effect.getEffect(), 100, effect.getAmplifier())));
        }
    }

    @Override
    public BlockEntityType<?> getType() {
        return PPBlockEntities.FIRE_RING.get();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        CompoundTag effects = tag.getCompound("Effects");
        for (String string : effects.getAllKeys()) {
            CompoundTag effect = effects.getCompound(string);
            this.effects.add(MobEffectInstance.load(effect));
        }
        if (!this.effects.isEmpty())
            this.potionColor = PotionUtils.getColor(this.effects);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag effects = new CompoundTag();
        int i = 0;
        for (MobEffectInstance effectInstance : this.effects) {
            CompoundTag effect = new CompoundTag();
            effectInstance.save(effect);
            effects.put(String.valueOf(i), effect);
            i++;
        }
        tag.put("Effects", effects);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("PotionColor", this.potionColor);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains("PotionColor", CompoundTag.TAG_INT))
            this.potionColor = tag.getInt("PotionColor");
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        super.onDataPacket(connection, packet);
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            handleUpdateTag(tag);
            if (this.level != null) {
                BlockState state = level.getBlockState(worldPosition);
                level.sendBlockUpdated(worldPosition, state, state, 3);
            }
        }
    }
}
