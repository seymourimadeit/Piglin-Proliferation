package tallestred.piglinproliferation.mixins;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestred.piglinproliferation.common.tags.PPTags;

@Mixin(HugeFungusFeature.class)
public abstract class HugeFungusFeatureMixin extends Feature<HugeFungusConfiguration> {
    public HugeFungusFeatureMixin(Codec<HugeFungusConfiguration> pCodec) {
        super(pCodec);
    }

    @Inject(at = @At(value = "HEAD"), method = "place", cancellable = true)
    protected void place(FeaturePlaceContext<HugeFungusConfiguration> pContext, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        WorldGenLevel worldgenlevel = pContext.level();
        BlockPos pos = pContext.origin();
        Registry<Structure> configuredStructureFeatureRegistry = worldgenlevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
        StructureManager structureManager = worldgenlevel.getLevel().structureManager();

        for (Holder<Structure> configuredStructureFeature : configuredStructureFeatureRegistry.getOrCreateTag(PPTags.TRAVELER_CAMPS)) {
            if (structureManager.getStructureAt(pos, configuredStructureFeature.value()).isValid()) {
                callbackInfoReturnable.setReturnValue(false);
                return;
            }
        }
    }
}
