package tallestred.piglinproliferation.capablities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tallestred.piglinproliferation.PiglinProliferation;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID)
public class TransformationSourceProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(PiglinProliferation.MODID, "transformation_source");
    private final TransformationSourceListener.TransformationSource backend = new TransformationSourceListener.TransformationSource();
    private final LazyOptional<TransformationSourceListener> optionalData = LazyOptional.of(() -> backend);
    public TransformationSourceProvider() {
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return PPCapablities.TRANSFORMATION_SOURCE_TRACKER.orEmpty(cap, this.optionalData);
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
