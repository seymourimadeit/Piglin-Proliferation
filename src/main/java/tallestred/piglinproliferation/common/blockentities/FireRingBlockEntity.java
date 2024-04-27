package tallestred.piglinproliferation.common.blockentities;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import tallestred.piglinproliferation.common.advancement.PPCriteriaTriggers;
import tallestred.piglinproliferation.common.blocks.FireRingBlock;

import javax.annotation.Nullable;
import java.util.*;

public class FireRingBlockEntity extends CampfireBlockEntity {
    private final List<MobEffectInstance> effects = new ArrayList<>(); //Only on server
    private boolean hasEffects = false; //Synced to client, substitute for effects.isEmpty()
    private final List<ParticleOptions> particles = new ArrayList<>(); //Synced to client
    private int unsafeLightLevel = -1;

    public FireRingBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    private int getLightLevel() {
        if (this.unsafeLightLevel < 0) {
            BlockState state = this.getBlockState();
            BlockPos pos = this.getBlockPos();
            if (this.level != null)
                this.unsafeLightLevel = state.getBlock().getLightEmission(state, this.level, pos);
        }
        return this.unsafeLightLevel;
    }

    public boolean addEffects(@Nullable Player player, @Nullable InteractionHand hand, @Nullable ItemStack stack, Iterable<MobEffectInstance> effectsToAdd) {
        if (this.level != null && this.getBlockState().getValue(FireRingBlock.LIT)) {
            if (effectsToAdd.iterator().hasNext() && (this.level.isClientSide ? !this.hasEffects : this.effects.isEmpty())) {
                boolean hasInstantaneousEffects = false;
                for (MobEffectInstance effect : effectsToAdd)
                    if (effect.getEffect().value().isInstantenous())
                        hasInstantaneousEffects = true;
                if (!hasInstantaneousEffects) {
                    if (!this.level.isClientSide) {
                        if (player != null) {
                            PPCriteriaTriggers.ADD_EFFECT_TO_FIRE_RING.get().trigger((ServerPlayer) player);
                            if (stack != null) {
                                if (!player.getAbilities().instabuild) {
                                    stack.shrink(1);
                                    ItemStack result = ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE));
                                    if (stack.isEmpty() && hand != null)
                                        player.setItemInHand(hand, result);
                                }
                                this.level.playSound(null, this.getBlockPos(), SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                        }
                        effectsToAdd.forEach(effectInstance -> effects.add(new MobEffectInstance(effectInstance)));
                    } else {
                        for (MobEffectInstance effect : effectsToAdd)
                            if (effect.isVisible())
                                particles.add(effect.getParticleOptions());
                        this.hasEffects = true;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static void particleTick(Level level, BlockPos pos, BlockState state, FireRingBlockEntity blockEntity) {
        RandomSource random = level.random;
        if (!blockEntity.particles.isEmpty()) {
            level.addParticle(Util.getRandom(blockEntity.particles, random), (double) pos.getX() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1), (double) pos.getY() + random.nextDouble() + random.nextDouble(), (double) pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1), 1.0, 1.25, 1.0);
        }
        CampfireBlockEntity.particleTick(level, pos, state, blockEntity);
    }

    public static void cookTick(Level level, BlockPos pos, BlockState state, FireRingBlockEntity blockEntity, int tempEffectTime) {
        potionTick(level, pos, state, blockEntity, tempEffectTime);
        CampfireBlockEntity.cookTick(level, pos, state, blockEntity);
    }

    public static void cooldownTick(Level level, BlockPos pos, BlockState state, FireRingBlockEntity blockEntity) {
        if (!blockEntity.effects.isEmpty()) {
            blockEntity.effects.clear();
            blockEntity.syncToClient();
        }
        CampfireBlockEntity.cooldownTick(level, pos, state, blockEntity);
    }

    public static void potionTick(Level level, BlockPos pos, BlockState state, FireRingBlockEntity blockEntity, int tempEffectTime) {
        if (!blockEntity.effects.isEmpty()) {
            List<MobEffectInstance> toRemove = new ArrayList<>();
            for (MobEffectInstance effectInstance : blockEntity.effects) {
                effectInstance.tickDownDuration();
                if (effectInstance.getDuration() <= 0)
                    toRemove.add(effectInstance);
            }
            if (!toRemove.isEmpty() || blockEntity.particles.isEmpty()) {
                blockEntity.effects.removeAll(toRemove);
                blockEntity.syncToClient();
            }
            if (!blockEntity.effects.isEmpty()) {
                double radius = blockEntity.getLightLevel();
                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius)))
                    blockEntity.effects.forEach(effect -> {
                        entity.addEffect(new MobEffectInstance(effect.getEffect(), tempEffectTime, effect.getAmplifier(), true, effect.isVisible()));
                    });
            }
            blockEntity.setChanged();
        }
    }

    public void syncToClient() {
        this.hasEffects = !this.effects.isEmpty();
        particles.clear();
        for (MobEffectInstance effect : effects)
            if (effect.isVisible())
                particles.add(effect.getParticleOptions());
        if (this.level != null)
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        this.setChanged();
    }

    @Override
    public BlockEntityType<?> getType() {
        return PPBlockEntities.FIRE_RING.get();
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.effects.clear();
        ListTag effectsTag = tag.getList("Effects", Tag.TAG_COMPOUND);
        for (Tag effectTag : effectsTag)
            if (effectTag instanceof CompoundTag compoundTag)
                this.effects.add(MobEffectInstance.load(compoundTag));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ListTag effectsTag = new ListTag();
        for (MobEffectInstance effectInstance : this.effects)
            effectsTag.add(effectInstance.save());
        tag.put("Effects", effectsTag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        ListTag particlesTag = new ListTag();
        for (ParticleOptions options : particles)
            ParticleTypes.CODEC.encodeStart(NbtOps.INSTANCE, options).result().ifPresent(particlesTag::add);
        tag.put("Particles", particlesTag);
        tag.putBoolean("HasEffects", this.hasEffects);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        if (tag.contains("Particles", Tag.TAG_LIST)) {
            ListTag particlesTag = tag.getList("Particles", Tag.TAG_COMPOUND);
            this.particles.clear();
            particlesTag.forEach(pTag -> ParticleTypes.CODEC.parse(NbtOps.INSTANCE, pTag).result().ifPresent(this.particles::add));
        }
        if (tag.contains("HasEffects"))
            this.hasEffects = tag.getBoolean("HasEffects");
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider provider) {
        super.onDataPacket(net, packet, provider);
        handleUpdateTag(packet.getTag(), provider);
        if (this.level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }
}
