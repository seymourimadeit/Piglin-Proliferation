package tallestred.piglinproliferation.common.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import tallestred.piglinproliferation.PPMemoryModules;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.entities.ai.PiglinAlchemistAi;
import tallestred.piglinproliferation.configuration.PPConfig;
import tallestred.piglinproliferation.networking.AlchemistBeltSyncPacket;
import tallestred.piglinproliferation.networking.PPNetworking;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PiglinAlchemist extends Piglin {
    // Used for displaying holding animation
    protected static final EntityDataAccessor<Boolean> IS_ABOUT_TO_THROW_POTION = SynchedEntityData.defineId(PiglinAlchemist.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<ItemStack> ITEM_SHOWN_ON_OFFHAND = SynchedEntityData.defineId(PiglinAlchemist.class, EntityDataSerializers.ITEM_STACK);
    public final NonNullList<ItemStack> beltInventory = NonNullList.withSize(6, ItemStack.EMPTY);
    protected int arrowsShot;

    public PiglinAlchemist(EntityType<? extends PiglinAlchemist> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Piglin.createAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public static boolean checkChemistSpawnRules(EntityType<PiglinAlchemist> p_219198_, LevelAccessor p_219199_, MobSpawnType p_219200_, BlockPos p_219201_, RandomSource p_219202_) {
        return !p_219199_.getBlockState(p_219201_.below()).is(Blocks.NETHER_WART_BLOCK);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return PPSounds.ALCHEMIST_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return PPSounds.ALCHEMIST_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return PPSounds.ALCHEMIST_DEATH.get();
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource source, DifficultyInstance pDifficulty) {
        super.populateDefaultEquipmentSlots(source, pDifficulty);
        if (this.isAdult()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            if (source.nextFloat() < PPConfig.COMMON.healingArrowChances.get().floatValue()) {
                ItemStack tippedArrow = PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW, source.nextInt(PPConfig.COMMON.healingArrowMinStackSize.get(), PPConfig.COMMON.healingArrowMaxStackSize.get())), Potions.STRONG_HEALING);
                this.setBeltInventorySlot(source.nextInt(6), tippedArrow);
            }
            for (int slot = 0; slot < this.beltInventory.size(); slot++) {
                if (this.beltInventory.get(slot).isEmpty()) {
                    Potion effect = source.nextFloat() < 0.35F ? Potions.FIRE_RESISTANCE : source.nextFloat() < 0.30F ? Potions.STRONG_REGENERATION : source.nextFloat() < 0.25F ? Potions.STRONG_HEALING : Potions.STRONG_STRENGTH;
                    ItemStack potion = PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), effect);
                    this.setBeltInventorySlot(slot, potion);
                }
            }
        }
    }

    @Override
    protected void playStepSound(BlockPos p_32159_, BlockState p_32160_) {
        if (this.getRandom().nextInt(20) == 0 && this.beltInventory.stream().filter(itemStack -> itemStack.getItem() instanceof PotionItem).findAny().isPresent())
            this.playSound(PPSounds.ALCHEMIST_WALK.get(), 0.5F * (this.beltInventory.stream().filter(itemStack -> itemStack.getItem() instanceof PotionItem).count() * 0.5F), 1.0F);
        this.playSound(PPSounds.ALCHEMIST_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ABOUT_TO_THROW_POTION, false);
        this.entityData.define(ITEM_SHOWN_ON_OFFHAND, ItemStack.EMPTY);
    }

    @Override
    protected void onEffectAdded(MobEffectInstance mobEffect, @Nullable Entity entity) {
        if (mobEffect.getEffect() == MobEffects.FIRE_RESISTANCE) {
            this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
            this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
            this.setPathfindingMalus(BlockPathTypes.LAVA, 0.0F);
        }
        super.onEffectAdded(mobEffect, entity);
    }

    @Override
    protected void onEffectRemoved(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance.getEffect() == MobEffects.FIRE_RESISTANCE) {
            this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
            this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
            this.setPathfindingMalus(BlockPathTypes.LAVA, -1.0F);
        }
        super.onEffectRemoved(mobEffectInstance);
    }

    public boolean isGonnaThrowPotion() {
        return this.entityData.get(IS_ABOUT_TO_THROW_POTION);
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
        PiglinAlchemistAi.updateActivity(this);
    }

    @Override
    protected void playConvertedSound() {
        this.playSound(PPSounds.ALCHEMIST_CONVERTED.get(), this.getSoundVolume(), this.getVoicePitch() / 0.10F);
    }

    @Override
    public void playSoundEvent(SoundEvent sound) {
        this.playSound(sound, this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    public boolean canHunt() {
        return super.canHunt();
    }

    public void willThrowPotion(boolean throwPotion) {
        this.entityData.set(IS_ABOUT_TO_THROW_POTION, throwPotion);
    }

    public ItemStack getItemShownOnOffhand() {
        return this.entityData.get(ITEM_SHOWN_ON_OFFHAND);
    }

    public void setItemShownOnOffhand(ItemStack itemShownOnOffhand) {
        this.entityData.set(ITEM_SHOWN_ON_OFFHAND, itemShownOnOffhand);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
        for (ItemStack itemStack : this.beltInventory) {
            if (!itemStack.isEmpty()) {
                if (!EnchantmentHelper.hasVanishingCurse(itemStack) && this.getRandom().nextFloat() < PPConfig.COMMON.alchemistPotionChance.get()) {
                    this.spawnAtLocation(itemStack);
                } else {
                    if (itemStack.getItem() instanceof PotionItem) {
                        for (int i = 0; i < 5; ++i) {
                            BlockPos blockpos = this.blockPosition();
                            ((ServerLevel) this.level()).sendParticles((new ItemParticleOption(ParticleTypes.ITEM, itemStack)), (double) blockpos.getX() + level().random.nextDouble(), (double) (blockpos.getY() + 1), (double) blockpos.getZ() + level().random.nextDouble(), 0, 0.0D, 0.0D, 0.0D, 0.0D);
                        }
                        this.playSound(SoundEvents.SPLASH_POTION_BREAK, 0.5F, 1.0F);
                    }
                }
            }
        }
        Entity entity = pSource.getEntity();
        if (entity instanceof Creeper creeper) {
            if (creeper.canDropMobsSkull()) {
                creeper.increaseDroppedSkulls();
                this.spawnAtLocation(PPItems.PIGLIN_ALCHEMIST_HEAD_ITEM.get());
            }
        }
        if (pSource.getDirectEntity() instanceof Fireball fireball && fireball.getOwner() instanceof Ghast) {
            this.spawnAtLocation(PPItems.PIGLIN_ALCHEMIST_HEAD_ITEM.get());
        }
        this.beltInventory.clear();
        super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
    }

    public void throwPotion(ItemStack thrownPotion, float xRot, float yRot) {
        ThrownPotion thrownpotion = new ThrownPotion(this.level(), this);
        thrownpotion.setItem(thrownPotion);
        thrownpotion.shootFromRotation(this, xRot, yRot, -20.0F, 0.5F, 1.0F);
        if (!this.isSilent())
            this.level().playSound((Player) null, this.getX(), this.getY(), this.getZ(), SoundEvents.SPLASH_POTION_THROW, this.getSoundSource(), 1.0F, 0.8F + this.getRandom().nextFloat() * 0.4F);
        this.level().addFreshEntity(thrownpotion);
        this.willThrowPotion(false);
        thrownPotion.shrink(1);
    }

    protected Brain.Provider<PiglinAlchemist> alchemistBrainProvider() {
        List<MemoryModuleType<?>> ALCHEMIST_MEMORY_TYPES = new ArrayList<>(Piglin.MEMORY_TYPES);
        ALCHEMIST_MEMORY_TYPES.add(PPMemoryModules.POTION_THROW_TARGET.get()); // Fixes #4 and adds instant compat to mods who add new piglin memory types
        return Brain.provider(ImmutableList.copyOf(ALCHEMIST_MEMORY_TYPES), SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> p_34723_) {
        return PiglinAlchemistAi.makeBrain(this, this.alchemistBrainProvider().makeBrain(p_34723_));
    }

    @Override
    public boolean isBaby() {
        return false;
    }


    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.getMainHandItem().getItem() instanceof CrossbowItem)
            super.performRangedAttack(target, distanceFactor);
        if (this.getMainHandItem().getItem() instanceof BowItem) {
            ItemStack itemstack = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem)));
            for (int slot = 0; slot < this.beltInventory.size() && !(this.getItemShownOnOffhand().getItem() instanceof TippedArrowItem); slot++) {
                if (beltInventory.get(slot).getItem() instanceof TippedArrowItem) {
                    List<MobEffectInstance> effectInstanceList = PotionUtils.getPotion(beltInventory.get(slot)).getEffects();
                    if (this.getTarget() != null && target == this.getTarget() && effectInstanceList.stream().filter(mobEffectInstance -> !mobEffectInstance.getEffect().isBeneficial()).findAny().isPresent() || target != this.getTarget() || this.getTarget() != null && this.getTarget().isInvertedHealAndHarm() && effectInstanceList.stream().filter(mobEffectInstance -> mobEffectInstance.getEffect() == MobEffects.HEAL).findAny().isPresent()) {
                        this.setItemShownOnOffhand(beltInventory.get(slot).copy());
                        this.beltInventory.set(slot, ItemStack.EMPTY);
                    }
                }
            }
            if (this.getItemShownOnOffhand().getItem() instanceof TippedArrowItem)
                itemstack = this.getItemShownOnOffhand();
            AbstractArrow abstractarrowentity = ProjectileUtil.getMobArrow(this, itemstack, distanceFactor);
            if (this.getMainHandItem().getItem() instanceof net.minecraft.world.item.BowItem)
                abstractarrowentity = ((net.minecraft.world.item.BowItem)this.getMainHandItem().getItem()).customArrow(abstractarrowentity);
            int powerLevel = itemstack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
            if (powerLevel > 0)
                abstractarrowentity
                        .setBaseDamage(abstractarrowentity.getBaseDamage() + (double) powerLevel * 0.5D + 0.5D);
            int punchLevel = itemstack.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
            if (punchLevel > 0)
                abstractarrowentity.setKnockback(punchLevel);
            if (itemstack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0)
                abstractarrowentity.setSecondsOnFire(100);
            double d0 = target.getX() - this.getX();
            double d1 = target.getY(0.3333333333333333D) - abstractarrowentity.getY();
            double d2 = target.getZ() - this.getZ();
            double d3 = Mth.sqrt((float) (d0 * d0 + d2 * d2));
            abstractarrowentity.shoot(d0, d1 + d3 * (double) 0.2F, d2, 1.6F,
                    (float) (14 - this.level().getDifficulty().getId() * 4));
            this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.level().addFreshEntity(abstractarrowentity);
            this.setArrowsShot(this.getArrowsShot() + 1);
            itemstack.shrink(1);
            if (itemstack.isEmpty() && this.getItemShownOnOffhand().is(itemstack.getItem())) {
                this.setItemShownOnOffhand(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        ListTag listnbt = new ListTag();
        for (ItemStack itemstack : this.beltInventory) {
            CompoundTag compoundtag = new CompoundTag();
            if (!itemstack.isEmpty()) {
                itemstack.save(compoundtag);
            }
            listnbt.add(compoundtag);
        }
        compound.put("BeltInventory", listnbt);
        compound.putInt("ArrowsShot", this.getArrowsShot());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("BeltInventory", 9)) {
            ListTag listtag = compound.getList("BeltInventory", 10);
            for (int i = 0; i < this.beltInventory.size(); ++i) {
                this.beltInventory.set(i, ItemStack.of(listtag.getCompound(i)));
            }
        }
        this.setArrowsShot(compound.getInt("ArrowsShot"));
    }

    public int getArrowsShot() {
        return this.arrowsShot;
    }

    public void setArrowsShot(int arrowsShot) {
        this.arrowsShot = arrowsShot;
    }

    @Override
    public void tick() {
        super.tick();
        this.syncBeltToClient(); // This is poo poo, and I will probably need to find a better method in the future
    }

    public void setBeltInventorySlot(int index, ItemStack stack) {
        this.beltInventory.set(index, stack);
        this.syncBeltToClient();
    }

    public void syncBeltToClient() {
        if (!this.level().isClientSide) {
            for (int i = 0; i < this.beltInventory.size(); i++) {
                PPNetworking.INSTANCE.send(new AlchemistBeltSyncPacket(this.getId(), i, this.beltInventory.get(i)), PacketDistributor.TRACKING_ENTITY.with(this));
            }
        }
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem item) {
        return item instanceof BowItem || super.canFireProjectileWeapon(item);
    }
}
