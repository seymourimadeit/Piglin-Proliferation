package tallestred.piglinproliferation.common.entities;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
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
import tallestred.piglinproliferation.networking.AlchemistBeltSyncPacket;
import tallestred.piglinproliferation.networking.PPNetworking;

import javax.annotation.Nullable;

public class PiglinAlchemist extends Piglin {
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
        this.goalSelector.addGoal(3, new RunAwayAfterThreeShots(this, 1.5D));
        this.goalSelector.addGoal(4, new AlchemistBowAttackGoal<>(this, 1.0D, 20, 15.0F));
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
            this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
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
    protected void populateDefaultEquipmentSlots(DifficultyInstance pDifficulty) {
        super.populateDefaultEquipmentSlots(pDifficulty);
        if (this.isAdult()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            this.setBeltInventorySlot(0, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HARMING));
            this.setBeltInventorySlot(1, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.FIRE_RESISTANCE));
            this.setBeltInventorySlot(2, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.LONG_NIGHT_VISION));
            this.setBeltInventorySlot(3, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.POISON));
            this.setBeltInventorySlot(4, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.LUCK));
            this.setBeltInventorySlot(5, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.SLOWNESS)); // DEBUG
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
