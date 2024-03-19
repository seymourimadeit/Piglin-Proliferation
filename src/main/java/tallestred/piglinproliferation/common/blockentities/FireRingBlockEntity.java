package tallestred.piglinproliferation.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import tallestred.piglinproliferation.common.blocks.FireRingBlock;

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
                if (player.getAbilities().instabuild) {
                    stack.shrink(1);
                    ItemStack result = ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE));
                    if (stack.isEmpty())
                        player.setItemInHand(hand, result);
                }
            }
            for (MobEffectInstance effectInstance : potion.getEffects())
                effects.add(new MobEffectInstance(effectInstance.getEffect(), Math.min(effectInstance.getDuration(), 30), effectInstance.getAmplifier()));
            if (!(this.level == null))
                this.level.playSound(null, this.getBlockPos(), SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }
        return false;
    }

    public static void newCookTick(Level level, BlockPos pos, BlockState state, FireRingBlockEntity blockEntity) {
        potionTick(level, pos, state, blockEntity);
        CampfireBlockEntity.cookTick(level, pos, state, blockEntity);
    }

    public static void newCooldownTick(Level level, BlockPos pos, BlockState state, FireRingBlockEntity blockEntity) {
        potionTick(level, pos, state, blockEntity);
        CampfireBlockEntity.cooldownTick(level, pos, state, blockEntity);
    }

    public static void potionTick(Level level, BlockPos pos, BlockState state, FireRingBlockEntity blockEntity) {
        Set<MobEffectInstance> tempEffects = new HashSet<>();
        for (MobEffectInstance effectInstance : blockEntity.effects) {
            effectInstance.tickDownDuration();
            tempEffects.add(effectInstance);
        }
        int radius = FireRingBlock.LIGHT_VALUE/2;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(x-radius, y-radius, z-radius, x+radius, y+radius, z+radius))) {
            tempEffects.forEach(effect -> entity.addEffect(new MobEffectInstance(effect.getEffect(), 60, effect.getAmplifier())));
        }
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
