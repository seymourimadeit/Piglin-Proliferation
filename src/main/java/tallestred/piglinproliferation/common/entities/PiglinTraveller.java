package tallestred.piglinproliferation.common.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tallestred.piglinproliferation.PPMemoryModules;
import tallestred.piglinproliferation.common.entities.ai.PiglinAlchemistAi;
import tallestred.piglinproliferation.common.entities.ai.PiglinTravellerAi;

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
        List<MemoryModuleType<?>> ALCHEMIST_MEMORY_TYPES = new ArrayList<>(Piglin.MEMORY_TYPES);
        return Brain.provider(ImmutableList.copyOf(ALCHEMIST_MEMORY_TYPES), SENSOR_TYPES);
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
