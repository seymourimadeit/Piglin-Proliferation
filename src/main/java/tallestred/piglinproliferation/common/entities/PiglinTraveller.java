package tallestred.piglinproliferation.common.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.common.entities.ai.PiglinTravellerAi;
import tallestred.piglinproliferation.common.items.PPItems;

import java.util.ArrayList;
import java.util.List;

public class PiglinTraveller extends Piglin {
    public PiglinTraveller(EntityType<? extends PiglinTraveller> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean canReplaceCurrentItem(ItemStack pCandidate) {
        EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(pCandidate);
        ItemStack itemstack = this.getItemBySlot(equipmentslot);
        return this.canReplaceCurrentItem(pCandidate, itemstack);
    }

    @Override
    public void holdInOffHand(ItemStack pStack) {
        if (pStack.isPiglinCurrency()) {
            this.setItemSlot(EquipmentSlot.OFFHAND, pStack);
            this.setGuaranteedDrop(EquipmentSlot.OFFHAND);
        } else {
            this.setItemSlotAndDropWhenKilled(EquipmentSlot.OFFHAND, pStack);
        }
    }

    @Override
    public void holdInMainHand(ItemStack pStack) {
        this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, pStack);
    }

    @Override
    public boolean canHunt() {
        return super.canHunt();
    }

    @Override
    public void playSoundEvent(SoundEvent pSoundEvent) {
        this.playSound(pSoundEvent, this.getSoundVolume(), this.getVoicePitch());
    }

    protected Brain.Provider<PiglinTraveller> travellerBrainProvider() {
        List<MemoryModuleType<?>> TRAVELLER_MEMORY_TYPES = new ArrayList<>(Piglin.MEMORY_TYPES);
        return Brain.provider(ImmutableList.copyOf(TRAVELLER_MEMORY_TYPES), SENSOR_TYPES);
    }

    public void playBarteringAnimation() {
        this.swing(InteractionHand.MAIN_HAND);
        this.swing(InteractionHand.OFF_HAND);
        Vec3 vec3 = new Vec3(((double) this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
        vec3 = vec3.xRot(-this.getXRot() * ((float) Math.PI / 180F));
        vec3 = vec3.yRot(-this.getYRot() * ((float) Math.PI / 180F));
        double d0 = (double) (-this.random.nextFloat()) * 0.6D - 0.3D;
        Vec3 vec31 = new Vec3(((double) this.random.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
        vec31 = vec31.xRot(-this.getXRot() * ((float) Math.PI / 180F));
        vec31 = vec31.yRot(-this.getYRot() * ((float) Math.PI / 180F));
        vec31 = vec31.add(this.getX(), this.getEyeY(), this.getZ());
        if (this.level() instanceof ServerLevel) //Forge: Fix MC-2518 spawnParticle is nooped on server, need to use server specific variant
            ((ServerLevel) this.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(PPItems.TRAVELLERS_COMPASS.get())), vec31.x, vec31.y, vec31.z, 1, vec3.x, vec3.y + 0.05D, vec3.z, 0.0D);
        else
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(PPItems.TRAVELLERS_COMPASS.get())), vec31.x, vec31.y, vec31.z, vec3.x, vec3.y + 0.05D, vec3.z);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isConverting()) {
            ++this.timeInOverworld;
        } else {
            this.timeInOverworld = 0;
        }
        if (this.timeInOverworld > 300 && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(this, EntityType.ZOMBIFIED_PIGLIN, (timer) -> this.timeInOverworld = timer)) {
            this.playConvertedSound();
            this.finishConversion((ServerLevel) this.level());
        }
        this.level().getProfiler().push("piglinBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();
        PiglinTravellerAi.updateActivity(this);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> p_34723_) {
        return PiglinTravellerAi.makeBrain(this, this.travellerBrainProvider().makeBrain(p_34723_));
    }

    @Override
    public boolean isBaby() {
        return false;
    }
}
