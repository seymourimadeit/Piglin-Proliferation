package tallestred.piglinproliferation.common.entities;

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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.entities.ai.PiglinAlchemistAi;
import tallestred.piglinproliferation.common.entities.ai.goals.*;
import tallestred.piglinproliferation.networking.AlchemistBeltSyncPacket;
import tallestred.piglinproliferation.networking.PPNetworking;

import java.util.List;

public class PiglinAlchemist extends Piglin {
    // Used for displaying holding animation
    protected static final EntityDataAccessor<Boolean> IS_ABOUT_TO_THROW_POTION = SynchedEntityData.defineId(PiglinAlchemist.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<ItemStack> ITEM_SHOWN_ON_OFFHAND = SynchedEntityData.defineId(PiglinAlchemist.class, EntityDataSerializers.ITEM_STACK);
    public final NonNullList<ItemStack> beltInventory = NonNullList.withSize(6, ItemStack.EMPTY);
    protected int arrowsShot;

    public PiglinAlchemist(EntityType<? extends PiglinAlchemist> p_34683_, Level p_34684_) {
        super(p_34683_, p_34684_);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Piglin.createAttributes().add(Attributes.MAX_HEALTH, 20.0D);
    }

    @Override
    public void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new HelpAlliesWithTippedArrowGoal(this, 1.0D, 20, 15.0F, PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), Potions.STRONG_HEALING), (piglin -> piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth())));
        this.goalSelector.addGoal(1, new ThrowPotionOnOthersGoal(this, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.FIRE_RESISTANCE), (alchemist) -> alchemist.isAlive(), (piglin) -> piglin.isAlive() && piglin.isOnFire()));
        this.goalSelector.addGoal(2, new ThrowPotionOnOthersGoal(this, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_REGENERATION), (alchemist) -> alchemist.isAlive(), (piglin) -> {
            List<AbstractPiglin> list = this.level.getEntitiesOfClass(AbstractPiglin.class, this.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
            if (!list.isEmpty()) {
                for (AbstractPiglin piglin1 : list) {
                    if (piglin1 != this) {
                        if (piglin1.getTarget() != null || this.getTarget() != null)
                            return list.size() > 2 && piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth(); // This makes it so alchemists don't
                        // attempt to throw a healing potion if theres 2 or less of them, as if they did it would make it so only one is attacking while the other is failing to throw the potion because the
                        // attacker would just keep pushing into them
                    }
                }
            }
            return piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth();
        }));
        this.goalSelector.addGoal(3, new ThrowPotionOnOthersGoal(this, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_HEALING), (alchemist) -> {
            return alchemist.isAlive();
        }, (piglin) -> {
            List<AbstractPiglin> list = this.level.getEntitiesOfClass(AbstractPiglin.class, this.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));
            if (!list.isEmpty()) {
                for (AbstractPiglin piglin1 : list) {
                    if (piglin1 != this) {
                        if (piglin1.getTarget() != null || this.getTarget() != null)
                            return piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth() && !this.beltInventory.stream().anyMatch(itemStack -> PotionUtils.getPotion(itemStack) == Potions.STRONG_REGENERATION) && list.size() > 2; // This makes it so alchemists don't
                        // attempt to throw a healing potion if theres only 2 or less of them, as if they did it would make it so only one is attacking while the other is failing to throw the potion because the
                        // attacker would just keep pushing into them
                    }
                }
            }
            return piglin.isAlive() && piglin.getHealth() < piglin.getMaxHealth();
        }));
        this.goalSelector.addGoal(4, new ThrowPotionOnOthersGoal(this, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_STRENGTH), (alchemist) -> {
            return alchemist.isAlive();
        }, (piglin) -> {
            return piglin.isAlive() && piglin.getTarget() != null && piglin.getHealth() < (piglin.getMaxHealth() / 2) && !piglin.isHolding((itemStack) -> {
                Item itemInStack = itemStack.getItem();
                return itemInStack instanceof ProjectileWeaponItem;
            });
        }));
        this.goalSelector.addGoal(5, new ThrowPotionOnSelfGoal(this, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.FIRE_RESISTANCE), (alchemist) -> {
            return alchemist.isAlive() && alchemist.isOnFire();
        }));
        this.goalSelector.addGoal(6, new ThrowPotionOnSelfGoal(this, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_REGENERATION), (alchemist) -> {
            return alchemist.isAlive() && alchemist.getHealth() < alchemist.getMaxHealth();
        }));
        this.goalSelector.addGoal(7, new ThrowPotionOnSelfGoal(this, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_HEALING), (alchemist) -> {
            return alchemist.isAlive() && alchemist.getHealth() < alchemist.getMaxHealth() && !this.beltInventory.stream().anyMatch(itemStack -> PotionUtils.getPotion(itemStack) == Potions.STRONG_REGENERATION);
        }));
        this.goalSelector.addGoal(8, new RunAwayAfterThreeShots(this, 1.5D));
        this.goalSelector.addGoal(9, new AlchemistBowAttackGoal<>(this, 1.0D, 20, 15.0F));
        this.goalSelector.addGoal(10, new MoveAroundLargeGroupsOfPiglinsGoal(this, 1.0D));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ABOUT_TO_THROW_POTION, false);
        this.entityData.define(ITEM_SHOWN_ON_OFFHAND, ItemStack.EMPTY);
    }

    public boolean isGonnaThrowPotion() {
        return this.entityData.get(IS_ABOUT_TO_THROW_POTION);
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
                if (!EnchantmentHelper.hasVanishingCurse(itemStack) && this.getRandom().nextFloat() < 0.20F) {
                    this.spawnAtLocation(itemStack);
                } else {
                    if (itemStack.getItem() instanceof PotionItem) {
                        for (int i = 0; i < 5; ++i) {
                            BlockPos blockpos = this.blockPosition();
                            ((ServerLevel) this.level).sendParticles((new ItemParticleOption(ParticleTypes.ITEM, itemStack)), (double) blockpos.getX() + level.random.nextDouble(), (double) (blockpos.getY() + 1), (double) blockpos.getZ() + level.random.nextDouble(), 0, 0.0D, 0.0D, 0.0D, 0.0D);
                        }
                        this.playSound(SoundEvents.SPLASH_POTION_BREAK, 0.5F, 1.0F);
                    }
                }
            }
        }
        this.beltInventory.clear();
        super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
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
            abstractarrowentity = ((net.minecraft.world.item.BowItem) this.getMainHandItem().getItem())
                    .customArrow(abstractarrowentity);
            int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, itemstack);
            if (powerLevel > 0)
                abstractarrowentity
                        .setBaseDamage(abstractarrowentity.getBaseDamage() + (double) powerLevel * 0.5D + 0.5D);
            int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, itemstack);
            if (punchLevel > 0)
                abstractarrowentity.setKnockback(punchLevel);
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, itemstack) > 0)
                abstractarrowentity.setSecondsOnFire(100);
            double d0 = target.getX() - this.getX();
            double d1 = target.getY(0.3333333333333333D) - abstractarrowentity.getY();
            double d2 = target.getZ() - this.getZ();
            double d3 = Mth.sqrt((float) (d0 * d0 + d2 * d2));
            abstractarrowentity.shoot(d0, d1 + d3 * (double) 0.2F, d2, 1.6F,
                    (float) (14 - this.level.getDifficulty().getId() * 4));
            this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.level.addFreshEntity(abstractarrowentity);
            this.setArrowsShot(this.getArrowsShot() + 1);
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

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance pDifficulty) {
        super.populateDefaultEquipmentSlots(pDifficulty);
        if (this.isAdult()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            if (this.getRandom().nextFloat() < 0.30F) {
                ItemStack tippedArrow = PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW, this.getRandom().nextInt(5)), Potions.STRONG_HEALING);
                this.setBeltInventorySlot(this.random.nextInt(6), tippedArrow);
            }
            for (int slot = 0; slot < this.beltInventory.size(); slot++) {
                if (this.beltInventory.get(slot).isEmpty()) {
                    Potion effect = this.getRandom().nextFloat() < 0.5F ? Potions.FIRE_RESISTANCE : this.getRandom().nextFloat() < 0.3F ? Potions.STRONG_REGENERATION : this.getRandom().nextFloat() < 0.25F ? Potions.STRONG_HEALING : Potions.STRONG_STRENGTH;
                    ItemStack potion = PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), effect);
                    this.setBeltInventorySlot(slot, potion);
                }
            }
        }
    }


    protected void playStepSound(BlockPos p_32159_, BlockState p_32160_) {
        if (this.getRandom().nextInt(5) == 0 && this.beltInventory.stream().filter(itemStack -> itemStack.getItem() instanceof PotionItem).findAny().isPresent())
            this.playSound(PPSounds.ALCHEMIST_WALK.get(), 0.8F * (this.beltInventory.stream().filter(itemStack -> itemStack.getItem() instanceof PotionItem).count() * 0.5F), 1.0F);
        super.playStepSound(p_32159_, p_32160_);
    }


    public void setBeltInventorySlot(int index, ItemStack stack) {
        this.beltInventory.set(index, stack);
        this.syncBeltToClient();
    }

    public void throwPotion(ItemStack thrownPotion, float xRot, float yRot) {
        ThrownPotion thrownpotion = new ThrownPotion(this.level, this);
        thrownpotion.setItem(thrownPotion);
        thrownpotion.shootFromRotation(this, xRot, yRot, -20.0F, 0.5F, 1.0F);
        if (!this.isSilent())
            this.level.playSound((Player) null, this.getX(), this.getY(), this.getZ(), SoundEvents.SPLASH_POTION_THROW, this.getSoundSource(), 1.0F, 0.8F + this.getRandom().nextFloat() * 0.4F);
        this.level.addFreshEntity(thrownpotion);
        this.willThrowPotion(false);
        thrownPotion.shrink(1);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> p_34723_) {
        return PiglinAlchemistAi.makeBrain(this, this.brainProvider().makeBrain(p_34723_));
    }

    public void syncBeltToClient() {
        if (!this.level.isClientSide) {
            for (int i = 0; i < this.beltInventory.size(); i++) {
                PPNetworking.INSTANCE.send(PacketDistributor.ALL.noArg(), new AlchemistBeltSyncPacket(this.getId(), i, this.beltInventory.get(i)));
            }
        }
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem item) {
        return item instanceof BowItem || super.canFireProjectileWeapon(item);
    }
}
