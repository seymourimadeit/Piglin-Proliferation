package tallestred.piglinproliferation.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import tallestred.piglinproliferation.common.blocks.FireRingBlock;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;

import javax.annotation.Nullable;
import java.util.*;

public class FireRingBlockEntity extends CampfireBlockEntity {
    private List<MobEffectInstance> effects = new ArrayList<>();

    public FireRingBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    public boolean addPotion(@Nullable Player player, InteractionHand hand, ItemStack stack, Potion potion) {
        if (this.effects.isEmpty()) {
            if (player != null) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    ItemStack result = ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE));
                    if (stack.isEmpty())
                        player.setItemInHand(hand, result);
                }
            }
            for (MobEffectInstance effectInstance : potion.getEffects())
                effects.add(new MobEffectInstance(effectInstance.getEffect(), Math.min(effectInstance.getDuration(), 600), effectInstance.getAmplifier()));
            if (!(this.level == null))
                this.level.playSound(null, this.getBlockPos(), SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }
        return false;
    }

    public static void particleTick(Level level, BlockPos pos, BlockState state, FireRingBlockEntity blockEntity) {
        if (!blockEntity.effects.isEmpty()) {
            RandomSource randomsource = level.getRandom();
            level.addAlwaysVisibleParticle(ParticleTypes.EFFECT, true, (double)pos.getX() + 0.5 + randomsource.nextDouble() / 3.0 * (double)(randomsource.nextBoolean() ? 1 : -1), (double)pos.getY() + randomsource.nextDouble() + randomsource.nextDouble(), (double)pos.getZ() + 0.5 + randomsource.nextDouble() / 3.0 * (double)(randomsource.nextBoolean() ? 1 : -1), 0.0, 0.07, 0.0);
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

    //TODO this doesn't work, potion colour needs to be sent to the client instead of checking the list
    public static void potionTick(Level level, BlockPos pos, BlockState state, FireRingBlockEntity blockEntity) {
        for (MobEffectInstance effectInstance : new ArrayList<>(blockEntity.effects)) {
            effectInstance.tickDownDuration();
            if (!effectInstance.isInfiniteDuration() && effectInstance.getDuration() <= 0)
                blockEntity.effects.remove(effectInstance);
        }
        int radius = FireRingBlock.LIGHT_VALUE/2;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(x-radius, y-radius, z-radius, x+radius, y+radius, z+radius))) {
            blockEntity.effects.forEach(effect -> entity.addEffect(new MobEffectInstance(effect.getEffect(), 60, effect.getAmplifier())));
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
}
