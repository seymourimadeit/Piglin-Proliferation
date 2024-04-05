package tallestred.piglinproliferation.capablities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tallestred.piglinproliferation.PiglinProliferation;

public class GuranteedCritProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(PiglinProliferation.MODID, "guaranteed_crit");
    private final CriticalAura.GuaranteedCriticalHit backend = new CriticalAura.GuaranteedCriticalHit();
    private final LazyOptional<CriticalAura> optionalData = LazyOptional.of(() -> backend);

    public GuranteedCritProvider() {
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return PPCapablities.GUARANTEED_CRIT_TRACKER.orEmpty(cap, this.optionalData);
    }

    public void invalidate() {
        this.optionalData.invalidate();
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.backend.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.backend.deserializeNBT(nbt);
    }
}
