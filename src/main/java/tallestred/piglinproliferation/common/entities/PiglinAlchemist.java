package tallestred.piglinproliferation.common.entities;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import tallestred.piglinproliferation.common.entities.ai.goals.AlchemistBowAttackGoal;
import tallestred.piglinproliferation.common.entities.ai.goals.RunAwayAfterThreeShots;
import tallestred.piglinproliferation.common.entities.ai.goals.ThrowPotionOnOthersGoal;
import tallestred.piglinproliferation.networking.AlchemistBeltSyncPacket;
import tallestred.piglinproliferation.networking.PPNetworking;

import javax.annotation.Nullable;

public class PiglinAlchemist extends Piglin {
    // Used for displaying holding animation
    protected static final EntityDataAccessor<Boolean> IS_ABOUT_TO_THROW_POTION = SynchedEntityData.defineId(PiglinAlchemist.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<ItemStack> POTION_ABOUT_TO_BE_THROWN = SynchedEntityData.defineId(PiglinAlchemist.class, EntityDataSerializers.ITEM_STACK);
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
        this.goalSelector.addGoal(0, new ThrowPotionOnOthersGoal(this, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.LONG_REGENERATION), (alchemist) -> {
            return alchemist.isAlive();
        }, (piglin) -> {
            return piglin.getHealth() < piglin.getMaxHealth();
        }));
        this.goalSelector.addGoal(0, new ThrowPotionOnOthersGoal(this, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.LONG_FIRE_RESISTANCE), (alchemist) -> {
            return alchemist.isAlive();
        }, (piglin) -> {
            return piglin.isOnFire();
        }));
        this.goalSelector.addGoal(0, new ThrowPotionOnOthersGoal(this, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.LONG_STRENGTH), (alchemist) -> {
            return alchemist.isAlive();
        }, (piglin) -> {
            return piglin.getTarget() != null && piglin.getHealth() < 15;
        }));
        this.goalSelector.addGoal(3, new RunAwayAfterThreeShots(this, 1.5D));
        this.goalSelector.addGoal(4, new AlchemistBowAttackGoal<>(this, 1.0D, 20, 15.0F));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ABOUT_TO_THROW_POTION, false);
        this.entityData.define(POTION_ABOUT_TO_BE_THROWN, ItemStack.EMPTY);
    }

    public boolean isGonnaThrowPotion() {
        return this.entityData.get(IS_ABOUT_TO_THROW_POTION);
    }

    public void willThrowPotion(boolean throwPotion) {
        this.entityData.set(IS_ABOUT_TO_THROW_POTION, throwPotion);
    }

    public ItemStack getPotionAboutToThrown() {
        return this.entityData.get(POTION_ABOUT_TO_BE_THROWN);
    }

    public void setPotionAboutToBeThrown(ItemStack itemAboutTobeThrown) {
        this.entityData.set(POTION_ABOUT_TO_BE_THROWN, itemAboutTobeThrown);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
        for (ItemStack itemStack : this.beltInventory) {
            if (!EnchantmentHelper.hasVanishingCurse(itemStack) && !itemStack.isEmpty()) {
                this.spawnAtLocation(itemStack);
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
            ItemStack itemstack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem));
            ItemStack hand = this.getMainHandItem();
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
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.setImmuneToZombification(true);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource source, DifficultyInstance pDifficulty) {
        super.populateDefaultEquipmentSlots(source, pDifficulty);
        if (this.isAdult()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            for (int slot = 0; slot < this.beltInventory.size(); slot++) {
                this.randomlyGenerateEffect(slot, 0.5F, Potions.LONG_FIRE_RESISTANCE);
                this.randomlyGenerateEffect(slot, 0.3F, Potions.LONG_STRENGTH);
                this.randomlyGenerateEffect(slot, 0.2F, Potions.LONG_REGENERATION);
            }
        }
    }

    protected void randomlyGenerateEffect(int slot, float chance, Potion effect) {
        if (this.getRandom().nextFloat() < chance) {
            ItemStack potion = PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), effect);
            this.setBeltInventorySlot(slot, potion);
        }
    }

    public void setBeltInventorySlot(int index, ItemStack stack) {
        this.beltInventory.set(index, stack);
        this.syncBeltToClient();
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
