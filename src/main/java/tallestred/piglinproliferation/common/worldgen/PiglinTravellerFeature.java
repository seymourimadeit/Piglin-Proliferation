package tallestred.piglinproliferation.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;

public class PiglinTravellerFeature extends Feature<NoneFeatureConfiguration> {

    public PiglinTravellerFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    /**
     * This exists because I didn't want to hardcode the armour and weapons of the travellers in the camp
     */
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        // move down to spawn at the jigsaw block calling this
        BlockPos position = context.origin();
        WorldGenLevel level = context.level();
        PiglinTraveller traveller = PPEntityTypes.PIGLIN_TRAVELLER.get().create(level.getLevel());
        if (traveller != null) {
            traveller.absMoveTo((double) position.getX() + 0.5D, position.getY(), (double) position.getZ() + 0.5D);
            traveller.finalizeSpawn(level, level.getCurrentDifficultyAt(position), MobSpawnType.STRUCTURE, null, null);
            level.addFreshEntityWithPassengers(traveller);
            return true;
        }
        return false;
    }
}