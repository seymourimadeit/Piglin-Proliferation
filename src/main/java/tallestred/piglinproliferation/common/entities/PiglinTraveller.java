package tallestred.piglinproliferation.common.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.entities.ai.PiglinTravellerAi;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.loot.CompassLocationMap;
import tallestred.piglinproliferation.configuration.PPConfig;

import java.util.*;

public class PiglinTraveller extends Piglin {
    protected static final EntityDataAccessor<Integer> KICK_TICKS = SynchedEntityData.defineId(PiglinTraveller.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> KICK_COOLDOWN = SynchedEntityData.defineId(PiglinTraveller.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(PiglinTraveller.class, EntityDataSerializers.BOOLEAN);
    public CompassLocationMap alreadyLocatedObjects = new CompassLocationMap();
    public Map.Entry<CompassLocationMap.SearchObject, BlockPos> currentlyLocatedObject;

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

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        GlobalPos globalpos = GlobalPos.of(this.level().dimension(), this.blockPosition());
        this.getBrain().setMemory(MemoryModuleType.HOME, globalpos);
        this.setItemSlot(EquipmentSlot.MAINHAND, (double) this.random.nextFloat() < PPConfig.COMMON.travellerCrossbowChance.get().floatValue() ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD));
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
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
    @Override
    public void tick() {
        if (this.getKickCoolDown() > 0)
            this.setKickCoolDown(this.getKickCoolDown() - 1);
        if (this.getKickTicks() > 0)
            this.setKickTicks(this.getKickTicks() - 1);
        super.tick();
    }


    protected Brain.Provider<PiglinTraveller> travellerBrainProvider() {
        List<MemoryModuleType<?>> TRAVELLER_MEMORY_TYPES = new ArrayList<>(Piglin.MEMORY_TYPES);
        return Brain.provider(ImmutableList.copyOf(TRAVELLER_MEMORY_TYPES), SENSOR_TYPES);
    }

    public void playBarteringAnimation() {
        this.swing(InteractionHand.MAIN_HAND, true);
        Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, ((double)this.random.nextFloat() - 0.5D) * 0.1D);
        vec3 = vec3.xRot(-this.getXRot() * ((float)Math.PI / 180F));
        vec3 = vec3.yRot(-this.getYRot() * ((float)Math.PI / 180F));
        double d0 = (double)(-this.random.nextFloat()) * 0.6D - 0.3D;
        Vec3 vec31 = new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.8D, d0, 1.0D + ((double)this.random.nextFloat() - 0.5D) * 0.4D);
        vec31 = vec31.yRot(-this.yBodyRot * ((float)Math.PI / 180F));
        vec31 = vec31.add(this.getX(), this.getEyeY() + 1.0D, this.getZ());
        if (this.level() instanceof ServerLevel) //Forge: Fix MC-2518 spawnParticle is nooped on server, need to use server specific variant
            ((ServerLevel) this.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.GOLD_INGOT)), vec31.x, vec31.y, vec31.z, 1, vec3.x, vec3.y + 0.05D, vec3.z, 0.0D);
        else
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.GOLD_INGOT)), vec31.x, vec31.y, vec31.z, vec3.x, vec3.y + 0.05D, vec3.z);
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
        PiglinTravellerAi.INSTANCE.updateBrainActivity(this);
        for (Map.Entry<CompassLocationMap.SearchObject, Integer> entry : new HashMap<>(this.alreadyLocatedObjects).entrySet()) {
            CompassLocationMap.SearchObject searchObject = entry.getKey();
            this.alreadyLocatedObjects.put(searchObject, entry.getValue()-1);
            if (this.alreadyLocatedObjects.get(searchObject) <= 0)
                this.alreadyLocatedObjects.remove(searchObject);
        }
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PiglinTravellerAi.INSTANCE.populateBrain(this, this.travellerBrainProvider().makeBrain(dynamic));
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    public static boolean checkTravellerSpawnRules(EntityType<PiglinTraveller> entityType, LevelAccessor p_219199_, MobSpawnType p_219200_, BlockPos p_219201_, RandomSource p_219202_) {
        return !p_219199_.getBlockState(p_219201_.below()).is(Blocks.NETHER_WART_BLOCK);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SITTING, false);
        this.entityData.define(KICK_COOLDOWN, 0);
        this.entityData.define(KICK_TICKS, 0);
    }

    public boolean isSitting() {
        return this.entityData.get(SITTING);
    }


    @Override
    protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
        Entity entity = pSource.getEntity();
        if (entity instanceof Creeper creeper) {
            if (creeper.canDropMobsSkull()) {
                creeper.increaseDroppedSkulls();
                this.spawnAtLocation(PPItems.PIGLIN_TRAVELLER_HEAD_ITEM.get());
            }
        }
        if (pSource.getDirectEntity() instanceof Fireball fireball && fireball.getOwner() instanceof Ghast) {
            this.spawnAtLocation(PPItems.PIGLIN_TRAVELLER_HEAD_ITEM.get());
        }
        super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
    }


    public void sit(boolean sit) {
        this.entityData.set(SITTING, sit);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return PPSounds.TRAVELLER_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return PPSounds.TRAVELLER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return PPSounds.TRAVELLER_DEATH.get();
    }

    @Override
    protected void playConvertedSound() {
        this.playSound(PPSounds.TRAVELLER_CONVERTED.get(), this.getSoundVolume(), this.getVoicePitch() / 0.10F);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.alreadyLocatedObjects = new CompassLocationMap(pCompound.getCompound("AlreadyLocatedObjects"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.put("AlreadyLocatedObjects", this.alreadyLocatedObjects.toNBT());
    }

    public int getKickTicks() {
        return this.entityData.get(KICK_TICKS);
    }

    public void setKickTicks(int kickTicks) {
        this.entityData.set(KICK_TICKS, kickTicks);
    }

    public int getKickCoolDown() {
        return this.entityData.get(KICK_COOLDOWN);
    }

    public void setKickCoolDown(int kickCoolDown) {
        this.entityData.set(KICK_COOLDOWN, kickCoolDown);
    }

}
