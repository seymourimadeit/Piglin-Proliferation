package tallestred.piglinproliferation.mixins;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tallestred.piglinproliferation.PPEvents;
import tallestred.piglinproliferation.capablities.TransformationSourceListener;

import java.util.Random;
import java.util.UUID;

@Mixin(ZombifiedPiglin.class)
public abstract class ZombifiedPiglinMixin extends Zombie {
    @Shadow
    @javax.annotation.Nullable
    private UUID persistentAngerTarget;

    public ZombifiedPiglinMixin(Level p_34274_) {
        super(p_34274_);
    }


    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_34297_, DifficultyInstance p_34298_, MobSpawnType p_34299_, @Nullable SpawnGroupData p_34300_, @Nullable CompoundTag p_34301_) {
        SpawnGroupData dataGroup = super.finalizeSpawn(p_34297_, p_34298_, p_34299_, p_34300_, p_34301_);
        Random randomSource = level.getRandom();
        TransformationSourceListener tSource = PPEvents.getTransformationSourceListener(this);
        if (p_34299_ != MobSpawnType.CONVERSION) {
            tSource.setTransformationSource("piglin");
            float bruteChance = 0.10F;
         /*   Registry<ConfiguredStructureFeature<?, ?>> registry = this.getLevel().registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
            HolderSet.Direct<ConfiguredStructureFeature<?, ?>> holderset = HolderSet.direct(registry.getHolderOrThrow(BuiltinStructures.BASTION_REMNANT));
            Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> pos = ((ServerLevel) this.getLevel()).getChunkSource().getGenerator().findNearestMapFeature((ServerLevel) this.getLevel(), holderset, this.blockPosition(), 50, false);
            if (pos != null) {
                BlockPos structurePos = new BlockPos(pos.getFirst().getX(), this.getY(), pos.getFirst().getZ());
                if (structurePos.closerToCenterThan(this.position(), 150.0D)) {
                    bruteChance += 0.15F;
                }
            }*/ // CAUSES WORLD DEADLOCK, USE CAUTION WHEN USING TOUCHING THIS
            if (randomSource.nextFloat() < bruteChance) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
                tSource.setTransformationSource("piglin_brute");
                if (ModList.get().isLoaded("bigbrain")) {
                    Item buckler = ForgeRegistries.ITEMS.getValue(new ResourceLocation("bigbrain:buckler"));
                    this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(buckler));
                }
            }
        }
        return dataGroup;
    }
}
