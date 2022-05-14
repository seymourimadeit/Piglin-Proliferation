package tallestred.piglinproliferation.common.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class PiglinAlchemist extends Piglin implements ContainerListener {
    private final SimpleContainer potionInventory = new SimpleContainer(6);
    private net.minecraftforge.common.util.LazyOptional<?> itemHandler;

    public PiglinAlchemist(EntityType<? extends PiglinAlchemist> p_34683_, Level p_34684_) {
        super(p_34683_, p_34684_);
        this.potionInventory.addListener(this);
        this.itemHandler = net.minecraftforge.common.util.LazyOptional
                .of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this.potionInventory));
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Piglin.createAttributes().add(Attributes.MAX_HEALTH, 20.0D);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        ListTag listnbt = new ListTag();
        for (int i = 0; i < this.getPotionInventory().getContainerSize(); ++i) {
            ItemStack itemstack = this.getPotionInventory().getItem(i);
            if (!itemstack.isEmpty()) {
                CompoundTag compoundnbt = new CompoundTag();
                compoundnbt.putByte("Slot", (byte) i);
                itemstack.save(compoundnbt);
                listnbt.add(compoundnbt);
            }
        }
        compound.put("PotionInventory", listnbt);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        ListTag listnbt = compound.getList("PotionInventory", 10);
        for (int i = 0; i < listnbt.size(); ++i) {
            CompoundTag compoundnbt = listnbt.getCompound(i);
            int j = compoundnbt.getByte("Slot") & 255;
            this.getPotionInventory().setItem(j, ItemStack.of(compoundnbt));
        }
    }

    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(
            net.minecraftforge.common.capabilities.Capability<T> capability,
            @Nullable net.minecraft.core.Direction facing) {
        if (this.isAlive() && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                && itemHandler != null)
            return itemHandler.cast();
        return super.getCapability(capability, facing);
    }

    public SimpleContainer getPotionInventory() {
        return this.potionInventory;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.getPotionInventory().setItem(0, new ItemStack(Items.ACACIA_FENCE));
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    public void containerChanged(Container pInvBasic) {
    }
}
