package tallestred.piglinproliferation.mixins;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.PPTags;
import tallestred.piglinproliferation.common.worldgen.PPWorldgen;

import javax.security.auth.callback.Callback;

@Mixin(NetherForestVegetationFeature.class)
public abstract class NetherForestVegetationFeatureMixin extends Feature {
    public NetherForestVegetationFeatureMixin(Codec pCodec) {
        super(pCodec);
    }

    @Inject(at = @At(value = "HEAD"), method = "place", cancellable = true)
    protected void place(FeaturePlaceContext<HugeFungusConfiguration> pContext, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        WorldGenLevel worldgenlevel = pContext.level();
        BlockPos pos = pContext.origin();
        if (worldgenlevel.getLevel().structureManager().getStructureWithPieceAt(pos, PPTags.TRAVELLER_BASES).isValid()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }
}
