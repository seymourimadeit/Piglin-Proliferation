package tallestred.piglinproliferation.common.entities;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import tallestred.piglinproliferation.networking.AlchemistBeltSyncPacket;
import tallestred.piglinproliferation.networking.PPNetworking;

import javax.annotation.Nullable;

public class PiglinAlchemist extends Piglin {
    public final NonNullList<ItemStack> beltInventory = NonNullList.withSize(6, ItemStack.EMPTY);

    public PiglinAlchemist(EntityType<? extends PiglinAlchemist> p_34683_, Level p_34684_) {
        super(p_34683_, p_34684_);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Piglin.createAttributes().add(Attributes.MAX_HEALTH, 20.0D);
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
        //this.syncBeltToClient();
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
    }

    @Override
    public void tick() {
        super.tick();
        this.syncBeltToClient(); // This is poo poo, and I will probably need to find a better method in the future
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.setBeltInventorySlot(0, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HARMING));
        this.setBeltInventorySlot(1, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.FIRE_RESISTANCE));
        this.setBeltInventorySlot(2, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.LONG_NIGHT_VISION));
        this.setBeltInventorySlot(3, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.POISON));
        this.setBeltInventorySlot(4, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HARMING));
        this.setBeltInventorySlot(5, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.SLOWNESS));
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance pDifficulty) {
        if (this.isAdult())
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
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
}
