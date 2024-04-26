package tallestred.piglinproliferation.common.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
import tallestred.piglinproliferation.PPMemoryModules;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.blocks.PiglinSkullBlock;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.entities.ai.PiglinAlchemistAi;
import tallestred.piglinproliferation.configuration.PPConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static tallestred.piglinproliferation.util.CodeUtilities.potionContents;

public class PiglinAlchemist extends Piglin {
    // Used for displaying holding animation
    protected static final EntityDataAccessor<Boolean> IS_ABOUT_TO_THROW_POTION = SynchedEntityData.defineId(PiglinAlchemist.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<ItemStack> ITEM_SHOWN_ON_OFFHAND = SynchedEntityData.defineId(PiglinAlchemist.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack>[] BELT_INVENTORY_SLOTS = defineBeltInventory(6);
    public final BeltInventory beltInventory = new BeltInventory();
    protected int arrowsShot;

    public PiglinAlchemist(EntityType<? extends PiglinAlchemist> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Piglin.createAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @SuppressWarnings("unused") //Needed for where it's called
    public static boolean checkChemistSpawnRules(EntityType<PiglinAlchemist> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource) {
        return !levelAccessor.getBlockState(blockPos.below()).is(Blocks.NETHER_WART_BLOCK);
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
                ItemStack tippedArrow = PotionContents.createItemStack(Items.TIPPED_ARROW, Potions.STRONG_HEALING);
                tippedArrow.setCount(source.nextInt(PPConfig.COMMON.healingArrowMinStackSize.get(), PPConfig.COMMON.healingArrowMaxStackSize.get()));
                this.beltInventory.set(source.nextInt(6), tippedArrow);
            }
            for (int slot = 0; slot < this.beltInventory.size(); slot++) {
                if (this.beltInventory.get(slot).isEmpty()) {
                    Holder<Potion> potion = source.nextFloat() < 0.35F ? Potions.FIRE_RESISTANCE : source.nextFloat() < 0.30F ? Potions.STRONG_REGENERATION : source.nextFloat() < 0.25F ? Potions.STRONG_HEALING : Potions.STRONG_STRENGTH;
                    this.beltInventory.set(slot, PotionContents.createItemStack(Items.SPLASH_POTION, potion));
                }
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return super.getAddEntityPacket();
    }

    @Override
    protected void playStepSound(BlockPos p_32159_, BlockState p_32160_) {
        if (this.getRandom().nextInt(20) == 0 && this.beltInventory.anyMatch(stack -> stack.getItem() instanceof PotionItem))
            this.playSound(PPSounds.ALCHEMIST_WALK.get(), 0.5F * (this.beltInventory.countMatches(stack -> stack.getItem() instanceof PotionItem) * 0.5F), 1.0F);
        this.playSound(PPSounds.ALCHEMIST_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ABOUT_TO_THROW_POTION, false);
        builder.define(ITEM_SHOWN_ON_OFFHAND, ItemStack.EMPTY);
        for (EntityDataAccessor<ItemStack> accessor : BELT_INVENTORY_SLOTS)
            builder.define(accessor, ItemStack.EMPTY);
    }

    @Override
    protected void onEffectAdded(MobEffectInstance mobEffect, @Nullable Entity entity) {
        if (mobEffect.getEffect() == MobEffects.FIRE_RESISTANCE) {
            this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
            this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
            this.setPathfindingMalus(PathType.LAVA, 0.0F);
        }
        super.onEffectAdded(mobEffect, entity);
    }

    @Override
    protected void onEffectRemoved(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance.getEffect() == MobEffects.FIRE_RESISTANCE) {
            this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
            this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
            this.setPathfindingMalus(PathType.LAVA, -1.0F);
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
        if (this.timeInOverworld > 300 && EventHooks.canLivingConvert(this, EntityType.ZOMBIFIED_PIGLIN, (timer) -> this.timeInOverworld = timer)) {
            this.playConvertedSound();
            this.finishConversion((ServerLevel) this.level());
        }
        this.level().getProfiler().push("piglinBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();
        PiglinAlchemistAi.INSTANCE.updateBrainActivity(this);
    }

    @Override
    protected void playConvertedSound() {
        this.playSound(PPSounds.ALCHEMIST_CONVERTED.get(), this.getSoundVolume(), this.getVoicePitch() / 0.10F);
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
        for (ItemStack itemStack : this.beltInventory.values()) {
            if (!itemStack.isEmpty()) {
                if (!EnchantmentHelper.hasVanishingCurse(itemStack) && this.getRandom().nextFloat() < PPConfig.COMMON.alchemistPotionChance.get()) {
                    this.spawnAtLocation(itemStack);
                } else {
                    if (itemStack.getItem() instanceof PotionItem) {
                        for (int i = 0; i < 5; ++i) {
                            BlockPos blockpos = this.blockPosition();
                            ((ServerLevel) this.level()).sendParticles((new ItemParticleOption(ParticleTypes.ITEM, itemStack)), (double) blockpos.getX() + level().random.nextDouble(), blockpos.getY() + 1, (double) blockpos.getZ() + level().random.nextDouble(), 0, 0.0D, 0.0D, 0.0D, 0.0D);
                        }
                        this.playSound(SoundEvents.SPLASH_POTION_BREAK, 0.5F, 1.0F);
                    }
                }
            }
        }
        PiglinSkullBlock.spawnSkullIfValidKill(pSource, this, e -> PPItems.PIGLIN_ALCHEMIST_HEAD_ITEM.get());
        this.beltInventory.clear();
        super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
    }

    public void throwPotion(ItemStack thrownPotion, float xRot, float yRot) {
        ThrownPotion thrownpotion = new ThrownPotion(this.level(), this);
        thrownpotion.setItem(thrownPotion);
        thrownpotion.shootFromRotation(this, xRot, yRot, -20.0F, 0.5F, 1.0F);
        if (!this.isSilent())
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SPLASH_POTION_THROW, this.getSoundSource(), 1.0F, 0.8F + this.getRandom().nextFloat() * 0.4F);
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
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PiglinAlchemistAi.INSTANCE.populateBrain(this, this.alchemistBrainProvider().makeBrain(dynamic));
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
                if (this.beltInventory.get(slot).getItem() instanceof TippedArrowItem) {
                    ItemStack tippedArrow = this.beltInventory.get(slot);
                    Iterable<MobEffectInstance> effectInstanceList = potionContents(tippedArrow).getAllEffects();
                    if (this.getTarget() != null && target == this.getTarget() && anyEffectsMatch(effectInstanceList, instance -> !instance.getEffect().value().isBeneficial()) || target != this.getTarget() || this.getTarget() != null && this.getTarget().isInvertedHealAndHarm() && anyEffectsMatch(effectInstanceList, instance -> instance.getEffect() == MobEffects.HEAL)) {
                        this.setItemShownOnOffhand(tippedArrow.copy());
                        this.beltInventory.set(slot, ItemStack.EMPTY);
                    }
                }
            }
            if (this.getItemShownOnOffhand().getItem() instanceof TippedArrowItem)
                itemstack = this.getItemShownOnOffhand();
            AbstractArrow abstractarrowentity = ProjectileUtil.getMobArrow(this, itemstack, distanceFactor);
            if (this.getMainHandItem().getItem() instanceof net.minecraft.world.item.BowItem)
                abstractarrowentity = ((net.minecraft.world.item.BowItem)this.getMainHandItem().getItem()).customArrow(abstractarrowentity, itemstack);
            int powerLevel = itemstack.getEnchantmentLevel(Enchantments.POWER);
            if (powerLevel > 0)
                abstractarrowentity
                        .setBaseDamage(abstractarrowentity.getBaseDamage() + (double) powerLevel * 0.5D + 0.5D);
            int punchLevel = itemstack.getEnchantmentLevel(Enchantments.PUNCH);
            if (punchLevel > 0)
                abstractarrowentity.setKnockback(punchLevel);
            if (itemstack.getEnchantmentLevel(Enchantments.FLAME) > 0)
                abstractarrowentity.igniteForSeconds(100);
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

    private boolean anyEffectsMatch(Iterable<MobEffectInstance> effectInstances, Predicate<MobEffectInstance> condition) {
        for (MobEffectInstance effectInstance : effectInstances)
            if (condition.test(effectInstance))
                return true;
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ContainerHelper.saveAllItems(tag, this.beltInventory.values(), this.registryAccess());
        tag.putInt("ArrowsShot", this.getArrowsShot());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        NonNullList<ItemStack> readItems = NonNullList.withSize(6, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, readItems, this.registryAccess());
        for (int i = 0; i < readItems.size(); ++i)
            this.beltInventory.set(i, readItems.get(i));
        this.setArrowsShot(tag.getInt("ArrowsShot"));
    }

    public int getArrowsShot() {
        return this.arrowsShot;
    }

    public void setArrowsShot(int arrowsShot) {
        this.arrowsShot = arrowsShot;
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem item) {
        return item instanceof BowItem || super.canFireProjectileWeapon(item);
    }

    @SuppressWarnings("unchecked")
    public static EntityDataAccessor<ItemStack>[] defineBeltInventory(int capacity) {
        EntityDataAccessor<ItemStack>[] slots = new EntityDataAccessor[capacity];
        for (int i = 0; i < capacity; i++)
            slots[i] = SynchedEntityData.defineId(PiglinAlchemist.class, EntityDataSerializers.ITEM_STACK);
        return slots;
    }

    public class BeltInventory {
        public NonNullList<ItemStack> values() {
            NonNullList<ItemStack> items = NonNullList.withSize(size(), ItemStack.EMPTY);
            for (int i = 0; i < size(); i++)
                items.set(i, entityData.get(BELT_INVENTORY_SLOTS[i]));
            return items;
        }

        public int size() {
            return BELT_INVENTORY_SLOTS.length;
        }

        public void clear() {
            for (int i = 0; i < size(); i++)
               entityData.set(BELT_INVENTORY_SLOTS[i], ItemStack.EMPTY);
        }

        public ItemStack get(int slot) {
           return entityData.get(BELT_INVENTORY_SLOTS[slot]);
        }

        public void set(int slot, ItemStack stack) {
            entityData.set(BELT_INVENTORY_SLOTS[slot], stack);
        }

        public boolean anyMatch(Predicate<ItemStack> predicate) {
            for (EntityDataAccessor<ItemStack> accessor : BELT_INVENTORY_SLOTS)
                if (predicate.test(entityData.get(accessor)))
                    return true;
            return false;
        }

        public boolean noneMatch(Predicate<ItemStack> predicate) {
            for (EntityDataAccessor<ItemStack> accessor : BELT_INVENTORY_SLOTS)
                if (predicate.test(entityData.get(accessor)))
                    return false;
            return true;
        }

        public int countMatches(Predicate<ItemStack> predicate) {
            int count = 0;
            for (EntityDataAccessor<ItemStack> accessor : BELT_INVENTORY_SLOTS)
                if (predicate.test(entityData.get(accessor)))
                    count++;
            return count;
        }

        public List<ItemStack> getMatches(Predicate<ItemStack> predicate) {
            List<ItemStack> items = new ArrayList<>();
            for (EntityDataAccessor<ItemStack> accessor : BELT_INVENTORY_SLOTS) {
                ItemStack stack = entityData.get(accessor);
                if (predicate.test(stack))
                    items.add(stack);
            }
            return items;
        }
    }
}
